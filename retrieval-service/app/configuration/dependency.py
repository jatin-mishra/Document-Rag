from functools import lru_cache
from fastapi import Depends
from .settings import Settings
from app.services.s3_service import S3Service


@lru_cache
def get_settings() -> Settings:
    return Settings()


def get_s3_service(settings: Settings = Depends(get_settings)) -> S3Service:
    return S3Service(settings)