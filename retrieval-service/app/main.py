from fastapi import FastAPI

from app.api.routers.documents import router as documents_router
from app.api.routers.health import router as health_router
from app.configuration.settings import get_settings


def create_app() -> FastAPI:
    settings = get_settings()
    app = FastAPI(title=settings.app_name, version=settings.app_version)
    app.include_router(documents_router)
    return app


app = create_app()

if __name__ == "__main__":
    import uvicorn

    settings = get_settings()
    uvicorn.run("app.main:app", host=settings.host, port=settings.port, reload=True)
