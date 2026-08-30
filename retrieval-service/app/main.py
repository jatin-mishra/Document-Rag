
import httpx
from fastapi import FastAPI
from contextlib import asynccontextmanager
from app.configuration.dependency import get_settings, build_client
from app.routers.documents import router as documents_router



@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    clients : dict[str, httpx.AsyncClient] = {
        name: build_client(cfg) for name, cfg in settings.services.items()
    }
    app.state.clients = clients 
    try:
        yield 
    finally:
        import asyncio 
        await asyncio.gather(*(c.aclose() for c in clients.values()))

def create_app() -> FastAPI:
    settings = get_settings()
    app = FastAPI(title=settings.app_name, version=settings.app_version, lifespan=lifespan)
    app.include_router(documents_router)
    return app


app = create_app()

if __name__ == "__main__":
    import uvicorn

    settings = get_settings()
    uvicorn.run("app.main:app", host=settings.host, port=settings.port, reload=True)
