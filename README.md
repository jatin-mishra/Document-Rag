# Document-Rag

A two-service Retrieval-Augmented Generation (RAG) system over documents. Upload a document, have it chunked and embedded asynchronously, then ask questions that are answered from the retrieved chunks.

| Service | Stack | Port | Role |
|---|---|---|---|
| [`retrieval-service/`](retrieval-service/) | Python 3.12, FastAPI, uv | 8000 | Document upload, query API, LLM answer generation |
| [`indexer/`](indexer/) | Java 25, Spring Boot 4, Temporal, Gradle | 9265 | Durable chunk + embed workflow, writes vector and keyword stores |

## How the pieces fit

```
          upload                 POST /api/v1/documents/index
 client ─────────▶ retrieval-service ───────────────────────▶ indexer
                        │  ▲                                    │
                        │  │ fetch document bytes               │ Temporal workflow
                        ▼  │                                    │  chunk ─▶ embed (Ollama)
                       S3 (LocalStack) ◀────────────────────────┤
                                                                ▼
          query                                       pgvector (Postgres)  +  Elasticsearch
 client ─────────▶ retrieval-service ─── similarity / keyword search ──▶ chunks ──▶ Anthropic ──▶ answer
```

1. **Upload**: `retrieval-service` stores the file in S3 and asks `indexer` to index it.
2. **Index**: `indexer` runs a Temporal workflow with two activities. *Chunking* pulls the document back from `retrieval-service` and splits it. *Embedding* calls Ollama and writes rows to the `chunk_vector_store` table in pgvector and to the `chunk_keyword_store` index in Elasticsearch.
3. **Query**: `retrieval-service` embeds the question, retrieves matching chunks, renders a prompt from Jinja templates and calls Anthropic for the answer.

## Shared contract

These values are set independently in each service and must agree:

| Concern | Where it lives | Value |
|---|---|---|
| Embedding dimension | `indexer/src/main/resources/application.yaml` → `vector-store.embedding-dimension`, and `vector(1024)` in `indexer/src/main/resources/db/chunk_vector_store.sql` | 1024 |
| Vector table | `chunk_vector_store` (schema in the SQL file above) | id = `documentId + chunk index` |
| Keyword index | `es.keyword-index` | `chunk_keyword_store` |
| Indexer → retrieval callback | `http-service-configurations.services.document-service.baseUrl` | `http://localhost:8000` |
| Retrieval → indexer call | `retrieval-service/app/clients/indexing/indexing_client.py` | `POST /api/v1/documents/index` |

Change the embedding model and you must change the dimension in the YAML, the SQL DDL, and the embedding config on the retrieval side together.

## Infrastructure the services expect

- PostgreSQL with the pgvector extension (for example `pgvector/pgvector:pg16`) on `localhost:5432`. Apply `indexer/src/main/resources/db/chunk_vector_store.sql` once by hand.
- Elasticsearch 8 on `localhost:9200`.
- Temporal dev server on `localhost:7233` (`temporal server start-dev`).
- Ollama on `localhost:11434` with an embedding model loaded.
- S3-compatible store. LocalStack on `localhost:4566`, region `ap-south-1`.
- An Anthropic API key for answer generation.

## Running locally

Start the infrastructure above, then in two terminals:

```bash
# indexer
cd indexer
./gradlew bootRun

# retrieval-service
cd retrieval-service
cp .env.example .env     # fill in keys and connection strings
uv sync
uv run uvicorn app.main:app --reload
```

Retrieval API docs: http://localhost:8000/docs

## Repository layout

Each service is self-contained with its own build, lockfile, Dockerfile or Gradle wrapper, tests and `.gitignore`. Nothing is shared at the build level. This root holds only the cross-cutting documentation.

- `indexer/docs/temporal-workflow-lifecycle.md` describes the indexing workflow in depth.
- `retrieval-service/naive_rag.md` describes the retrieval and generation design.

## History

This repository was formed by merging two earlier repositories, `jatin-mishra/RagIndexer` and `jatin-mishra/retrieval-service`, with their full commit history rewritten under `indexer/` and `retrieval-service/`. `git log --follow` works across the move.

## License

MIT. See [LICENSE](LICENSE).
