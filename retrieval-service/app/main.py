
import httpx
from fastapi import FastAPI
from contextlib import asynccontextmanager
from app.configuration.dependency import get_settings, get_async_engine, get_session_maker
from app.configuration.builder import build_client
from app.routers.documents import router as documents_router
from app.routers.query import router as query_router

settings = get_settings()

@asynccontextmanager
async def lifespan(app: FastAPI):

    # http clients
    clients : dict[str, httpx.AsyncClient] = {
        name: build_client(cfg) for name, cfg in settings.services.items()
    }
    app.state.clients = clients 

    # engine creation
    engine = get_async_engine(settings=settings)
    app.state.db_engine = engine
    app.state.db_sessionmaker = get_session_maker(engine)

    try:
        yield 
    finally:
        import asyncio 
        await asyncio.gather(*(c.aclose() for c in clients.values()))
        await engine.dispose()


def create_app() -> FastAPI:
    app = FastAPI(title=settings.app_name, version=settings.app_version, lifespan=lifespan)
    app.include_router(documents_router)
    app.include_router(query_router)
    return app


app = create_app()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.host, port=settings.port, reload=True)
