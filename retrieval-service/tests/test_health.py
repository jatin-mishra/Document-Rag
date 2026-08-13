from fastapi.testclient import TestClient

from app.core.config import get_settings
from app.main import app

client = TestClient(app)


def test_health_returns_200():
    response = client.get("/health")
    assert response.status_code == 200


def test_health_response_schema():
    settings = get_settings()
    response = client.get("/health")
    assert response.json() == {
        "status": "healthy",
        "service": settings.app_name,
        "version": settings.app_version,
    }


def test_openapi_docs_reachable():
    assert client.get("/docs").status_code == 200
    assert client.get("/openapi.json").status_code == 200


def test_unknown_route_404():
    assert client.get("/nope").status_code == 404


def test_wrong_method_405():
    assert client.post("/health").status_code == 405
