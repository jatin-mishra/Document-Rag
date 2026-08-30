from functools import lru_cache
import httpx
from fastapi import Depends, HTTPException, Request

from app.clients.http.retry_transport import RetryTransport
from .settings import Settings, HttpClientConfiguration
from app.services.s3_service import S3Service


@lru_cache
def get_settings() -> Settings:
    return Settings()

def get_s3_service(settings: Settings = Depends(get_settings)) -> S3Service:
    return S3Service(settings)


def build_client(cfg: HttpClientConfiguration) -> httpx.AsyncClient:
    limits = httpx.Limits(
        max_connections=cfg.max_connections,
        max_keepalive_connections=cfg.max_keepalive,
        keepalive_expiry=cfg.keepalive_expiry,
    )
    transport = RetryTransport(
        httpx.AsyncHTTPTransport(limits=limits, http2=cfg.http2),
        max_retries=cfg.max_retries,
        backoff_base=cfg.retry_backoff_base,
        backoff_max=cfg.retry_backoff_max,
        retry_status_codes=cfg.retry_status_codes,
    )
    timeout = httpx.Timeout(
        connect=cfg.connect_timeout,
        read=cfg.read_timeout,
        write=cfg.write_timeout,
        pool=cfg.pool_timeout,
    )
    return httpx.AsyncClient(
        transport=transport,
        timeout=timeout,
        base_url=cfg.base_url,       
    )


# use as: orderClient = Annotated[httpx.AsyncClient, Depends(service_client("orders"))]
def service_client(name: str): 
    def _dep(request: Request) -> httpx.AsyncClient:
        client = request.app.state.clients.get(name)
        if client is None:
            raise HTTPException(500, f"HTTP client for '{name}' not configured")
        return client 
    return _dep