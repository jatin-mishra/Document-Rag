# Embedding-Creation Pipeline for retrieval-service (Simple RAG — Step 2)

## Context

The retrieval-service repo is a Step-1 FastAPI scaffold (health check + S3 upload API) built to grow into a RAG app. This step adds the **embedding-creation half** of the RAG: chunk text, embed it with a locally-hosted open-source model, and store vectors in Postgres/pgvector. **No retrieval/query endpoints yet** (that's step 3).

Decisions made with the user:
- **Model/hosting**: local `sentence-transformers` — default `BAAI/bge-small-en-v1.5` (384-dim, strong MTEB score, fast on Apple Silicon via automatic MPS). No paid API, no data leaves the machine. Model name + dimension configurable.
- **Vector store**: Postgres + pgvector (via docker-compose locally).
- **Ingestion**: (a) new `POST /embeddings` endpoint for raw text, (b) hook into the existing `POST /uploads` flow so text-like uploaded files get chunked + embedded.
- **Backward compatibility**: upload must succeed even if embedding/DB fails (best-effort, status reported in response).

## Design decisions

1. **psycopg3 + pgvector adapter, no SQLAlchemy/alembic** — matches the scaffold's no-ceremony style (module functions + `@lru_cache` factories, no `Depends()`). Schema created by an idempotent `CREATE ... IF NOT EXISTS` function (dimension comes from settings, so schema lives in Python, not a static SQL file). Short-lived connection per DAO call — fine at this scale.
2. **Single `document_chunks` table**, no ANN index yet (exact scan is fine at learning scale; add HNSW later).
3. **Character-based sliding-window chunker** (default 800 chars / 150 overlap, configurable) in `app/utils/`.
4. **Lazy model load** via `@lru_cache get_embedding_model()` with a function-local `sentence_transformers` import — app boots fast; tests monkeypatch it and never import torch. `normalize_embeddings=True` on encode.
5. **S3 hook is synchronous + best-effort**: after successful S3 put, reuse the in-memory buffer (`seek(0)`), embed only text-like content (`text/plain`, `text/markdown`, or `.txt`/`.md` when content-type missing/octet-stream); any failure → log + `embedding.status="failed"`, upload still 200. Non-text → `"skipped"`. (Known limitation to note in README: presigned-URL uploads bypass the service and aren't embedded.)

## Files — ordered steps

**Step 0 — workflow files (recreate; get metric file reviewed before coding)**
- `tasks/todo.yml` — checklist of the steps below.
- `metric/test_cases_and_metrics.yml` — test cases + failure conditions (see Test plan).

**Step 1 — deps** (`pyproject.toml`, then `uv lock`/`uv sync`)
- `psycopg[binary]>=3.2`, `pgvector>=0.3`, `sentence-transformers>=3.0` (heavy: torch; first run downloads ~130MB model to `~/.cache/huggingface`).

**Step 2 — `docker-compose.yml`** (new, repo root): `pgvector/pgvector:pg16`, db `retrieval`, port 5432, named volume.

**Step 3 — `app/core/config.py`** (modify) + `.env.example`:
```python
database_url: str = Field(default="postgresql://postgres:postgres@localhost:5432/retrieval", validation_alias="DATABASE_URL")
embedding_model_name: str = Field(default="BAAI/bge-small-en-v1.5", validation_alias="EMBEDDING_MODEL")
embedding_dimension: int = Field(default=384, validation_alias="EMBEDDING_DIMENSION")
chunk_size_chars: int = Field(default=800, validation_alias="CHUNK_SIZE_CHARS")
chunk_overlap_chars: int = Field(default=150, validation_alias="CHUNK_OVERLAP_CHARS")
```

**Step 4 — `app/core/db.py`** (new): `connect()` (autocommit conn, pgvector types registered) + `@lru_cache ensure_schema()` (lru_cache doesn't cache exceptions → retries while DB is down). Schema:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE IF NOT EXISTS document_chunks (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id uuid NOT NULL,
    source_type text NOT NULL CHECK (source_type IN ('api', 's3')),
    source_ref text,
    chunk_index int NOT NULL,
    content text NOT NULL,
    content_hash text NOT NULL,
    embedding vector(384) NOT NULL,  -- dim from settings
    created_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (document_id, chunk_index)
);
```

**Step 5 — `app/core/embedding.py`** (new): `@lru_cache get_embedding_model()` (mirrors `get_s3_client()`).

**Step 6 — `app/utils/text_chunker.py`** (new): `chunk_text(text, chunk_size, overlap) -> list[str]`; `[]` for blank input; `ValueError` if `overlap >= chunk_size`.

**Step 7 — models + errors**
- `app/models/embedding.py` (new): `ChunkRecord`, `EmbeddingResult` dataclasses.
- `app/errors/dao_errors.py`: add `VectorStoreDaoError`.
- `app/errors/service_errors.py`: add `EmbeddingError`, `EmptyTextError`, `EmbeddingServiceError`.

**Step 8 — `app/daos/vector_store_dao.py`** (new): `insert_chunks(chunks) -> list[str]` — `ensure_schema()`, executemany INSERT RETURNING id, wraps psycopg errors in `VectorStoreDaoError` (mirrors `s3_dao.py`).

**Step 9 — `app/services/embedding_service.py`** (new): `embed_and_store(text, source_type, source_ref) -> EmbeddingResult` — chunk → `EmptyTextError` if none → encode (normalized) → sha256 content_hash → DAO; `VectorStoreDaoError` → `EmbeddingServiceError`.

**Step 10 — hook `app/services/upload_service.py`** (modify) + `app/models/upload.py`:
- New `UploadEmbeddingInfo` dataclass (`status: "embedded"|"skipped"|"failed"`, `document_id`, `chunk_count`), optional field on `UploadResult` (additive).
- After successful S3 put: `_maybe_embed(buffer, key, content_type, filename)` — text-like check, `buffer.seek(0)`, utf-8 decode (errors → skipped), `embed_and_store(..., source_type="s3", source_ref=key)`, broad except → log + `"failed"`. **Upload never breaks.**

**Step 11 — API layer**
- `app/dtos/embedding_dtos.py` (new): `EmbedTextRequest {text: min_length=1, source_ref?}`, `EmbedTextResponse {document_id, chunk_count, chunk_ids, model}`.
- `app/dtos/upload_dtos.py` (modify): optional `embedding` sub-model on `UploadResponse`.
- `app/api/controllers/embeddings_controller.py` (new): `EmptyTextError` → 422, `EmbeddingServiceError` → 502 "vector store unavailable" (mirrors `uploads_controller.py`).
- `app/api/routers/embeddings.py` (new): `APIRouter(prefix="/embeddings")`, `POST ""`.
- `app/api/controllers/uploads_controller.py` (modify): pass embedding info through.
- `app/main.py` (modify): register embeddings router. No startup DB init — app must boot with DB down.

**Step 12 — tests** (`tests/conftest.py` new: shared moto fixture + `fake_embedding_model` + `fake_vector_dao` monkeypatch fixtures; real model/torch never loaded in tests).

## Test plan (→ metric/test_cases_and_metrics.yml)

- **Chunker**: short text → 1 chunk; long text → overlap verified; blank → `[]`; `overlap >= chunk_size` → ValueError.
- **POST /embeddings** (fakes): valid text → 200 with ids/count/model, DAO got `source_type="api"` + 384-dim vectors; multi-chunk indices 0..n-1 same document_id; empty text → 422; whitespace-only → 422; DAO error → 502.
- **Upload hook** (moto + fakes): `.txt` upload → 200 + `embedding.status="embedded"` + `source_ref==key`; `.pdf` → `"skipped"`, DAO not called; embed raises → still 200 + `"failed"` + object in S3; DB down → still 200 + `"failed"`; **all existing upload tests pass unchanged**.
- **Optional integration** (gated `RUN_DB_TESTS=1`, compose DB): real insert, dims, idempotent `ensure_schema`.
- **Failure conditions**: any existing test breaks; upload 5xx caused by embedding path; restructuring (not just adding to) existing folders; real model download during unit tests.

## Verification

1. `docker compose up -d db`; `uv sync`.
2. `uv run pytest` (green, no DB/model needed) and `uv run ruff check .`
3. `RUN_DB_TESTS=1 uv run pytest` against compose DB.
4. `uv run uvicorn app.main:app` →
   - `curl -X POST localhost:8000/embeddings -H 'Content-Type: application/json' -d '{"text":"pgvector stores embeddings in postgres."}'` → 200 (first call slow: model load).
   - `curl -F 'file=@README.md;type=text/markdown' localhost:8000/uploads` (LocalStack) → 200 with `embedding.status:"embedded"`.
   - `docker compose exec db psql -U postgres -d retrieval -c "SELECT source_type, count(*) FROM document_chunks GROUP BY 1;"`.
5. `docker compose stop db` → repeat upload curl → still 200 with `embedding.status:"failed"` (backward compat proven).
