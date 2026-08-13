# retrieval-service

Step 1 boilerplate: a FastAPI service with a dummy health check. Structured so
future steps can add pluggable RAG strategies (indexing, query, storage,
agentic loop) without restructuring the base app.

## Local development

```bash
uv sync
uv run uvicorn app.main:app --reload
```

Health check: `curl localhost:8000/health`
Docs: http://localhost:8000/docs

## Tests & lint

```bash
uv run pytest -v
uv run ruff check .
```

## Docker

```bash
docker build -t retrieval-service .
docker run -p 8000:8000 retrieval-service
```
