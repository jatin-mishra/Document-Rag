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

## File upload (S3)

Two endpoints, backed by S3. Point at LocalStack locally by setting env vars
(see `.env.example`); unset `LOCALSTACK_ENDPOINT` and use real IAM
credentials in stage/prod — no code changes needed to switch.

```bash
AWS_DEFAULT_REGION=us-east-1
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
LOCALSTACK_ENDPOINT=http://localhost:4566
BUCKET=practice-bucket
```

Server-side proxy upload (file goes through this service):

```bash
curl -F file=@somefile.txt localhost:8000/uploads
```

Presigned URL (client uploads directly to S3):

```bash
curl -X POST localhost:8000/uploads/presigned-url \
  -H 'content-type: application/json' \
  -d '{"filename": "somefile.txt", "content_type": "text/plain"}'
# then, using the returned "url":
curl -X PUT --upload-file somefile.txt "<url>"
```

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
