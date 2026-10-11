
## Local development

```bash
uv sync
uv run uvicorn app.main:app --reload
```

Health check: `curl localhost:8000/health`
Docs: http://localhost:8000/docs
