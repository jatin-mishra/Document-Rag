import boto3
import pytest
from fastapi.testclient import TestClient
from moto import mock_aws

from app.core.config import get_settings
from app.core.s3 import get_s3_client
from app.main import app

client = TestClient(app)


@pytest.fixture(autouse=True)
def _s3_bucket():
    get_s3_client.cache_clear()
    with mock_aws():
        settings = get_settings()
        boto3.client("s3", region_name="us-east-1").create_bucket(
            Bucket=settings.s3_bucket_name
        )
        yield
    get_s3_client.cache_clear()


def test_upload_file_success():
    response = client.post(
        "/uploads",
        files={"file": ("hello.txt", b"hello world", "text/plain")},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["bucket"] == get_settings().s3_bucket_name
    assert body["filename"] == "hello.txt"
    assert body["content_type"] == "text/plain"
    assert body["size_bytes"] == len(b"hello world")
    assert body["key"].startswith("uploads/")
    assert body["key"].endswith("hello.txt")

    settings = get_settings()
    stored = boto3.client("s3", region_name="us-east-1").get_object(
        Bucket=settings.s3_bucket_name, Key=body["key"]
    )
    assert stored["Body"].read() == b"hello world"


def test_upload_empty_file():
    response = client.post(
        "/uploads",
        files={"file": ("empty.txt", b"", "text/plain")},
    )
    assert response.status_code == 200
    assert response.json()["size_bytes"] == 0


def test_upload_file_exceeds_size_limit(monkeypatch):
    settings = get_settings()
    monkeypatch.setattr(settings, "s3_upload_max_bytes", 10)
    response = client.post(
        "/uploads",
        files={"file": ("big.txt", b"x" * 100, "text/plain")},
    )
    assert response.status_code == 413


def test_upload_missing_file_returns_422():
    response = client.post("/uploads")
    assert response.status_code == 422


def test_upload_surfaces_storage_error_as_502(monkeypatch):
    from app.daos import s3_dao
    from app.errors.dao_errors import S3DaoError

    def _boom(*args, **kwargs):
        raise S3DaoError("boom")

    monkeypatch.setattr(s3_dao, "upload_fileobj", _boom)
    response = client.post(
        "/uploads",
        files={"file": ("hello.txt", b"hello", "text/plain")},
    )
    assert response.status_code == 502


def test_presigned_url_returns_well_formed_response():
    response = client.post(
        "/uploads/presigned-url",
        json={"filename": "report.pdf", "content_type": "application/pdf"},
    )
    assert response.status_code == 200
    body = response.json()
    settings = get_settings()
    assert body["bucket"] == settings.s3_bucket_name
    assert body["method"] == "PUT"
    assert body["expires_in"] == settings.s3_presigned_url_expiry_seconds
    assert body["key"].startswith("uploads/")
    assert body["key"].endswith("report.pdf")
    assert body["key"] in body["url"]


def test_presigned_url_requires_filename():
    response = client.post("/uploads/presigned-url", json={})
    assert response.status_code == 422
