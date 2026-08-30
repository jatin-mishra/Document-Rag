from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import BaseModel

class S3Config(BaseModel):
    bucket: str 
    path: str 

class AwsConfig(BaseModel):
    url: str
    region: str 
    access_key: str 
    secret_key: str 
    s3: S3Config

class Settings(BaseSettings):
    app_name: str 
    app_version: str 
    aws: AwsConfig 

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        env_nested_delimiter="__",
        extra="ignore"
    )

