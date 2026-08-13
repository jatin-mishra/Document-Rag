from functools import lru_cache
from typing import Any

import boto3

from app.core.config import get_settings


@lru_cache
def get_s3_client() -> Any:
    settings = get_settings()
    return boto3.client("s3", endpoint_url=settings.s3_endpoint_url)
