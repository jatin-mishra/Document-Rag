from functools import lru_cache
import httpx
from typing import Annotated
from fastapi import Depends, HTTPException, Request
from .settings import Settings

from app.services.s3_service import S3Service


@lru_cache
def get_settings() -> Settings:
    return Settings()

# use as: orderClient = Annotated[httpx.AsyncClient, Depends(service_client("orders"))]
def service_client(name: str): 
    def _dep(request: Request) -> httpx.AsyncClient:
        client = request.app.state.clients.get(name)
        if client is None:
            raise HTTPException(500, f"HTTP client for '{name}' not configured")
        return client 
    return _dep


def get_s3_service(request: Request) -> S3Service:
    return request.app.state.s3_service

S3Dep = Annotated[S3Service, Depends(get_s3_service)]