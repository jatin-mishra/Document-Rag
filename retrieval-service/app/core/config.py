from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    app_name: str = "retrieval-service"
    app_version: str = "0.1.0"
    environment: str = "local"
    host: str = "0.0.0.0"
    port: int = 8000
    log_level: str = "info"

    # S3 endpoint override for LocalStack; leave unset in stage/prod so boto3
    # talks to real AWS. Region and credentials are intentionally not
    # duplicated here — boto3 already resolves AWS_DEFAULT_REGION,
    # AWS_ACCESS_KEY_ID, and AWS_SECRET_ACCESS_KEY from the environment.
    s3_endpoint_url: str | None = Field(default=None, validation_alias="LOCALSTACK_ENDPOINT")
    s3_bucket_name: str = Field(default="practice-bucket", validation_alias="BUCKET")
    s3_upload_max_bytes: int = 25 * 1024 * 1024
    s3_presigned_url_expiry_seconds: int = 3600


@lru_cache
def get_settings() -> Settings:
    return Settings()
