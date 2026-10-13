from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import BaseModel

from app.configuration.llm_configuration import LLMConfiguration


class S3Config(BaseModel):
    bucket: str 
    path: str 

class AwsConfig(BaseModel):
    url: str
    region: str 
    access_key: str 
    secret_key: str 
    s3: S3Config

class HttpClientConfiguration(BaseModel):

    # --- global per service ---
    base_url: str 

    # --- Connection pool ---
    max_connections: int = 100          # total sockets across all hosts
    max_keepalive: int = 20             # idle sockets kept warm for reuse
    keepalive_expiry: float = 30.0      # seconds an idle socket survives

    # --- Timeouts (seconds) — granular, per-phase ---
    connect_timeout: float = 5.0        # TCP + TLS handshake
    read_timeout: float = 10.0          # waiting for response bytes
    write_timeout: float = 10.0         # sending request body
    pool_timeout: float = 5.0           # waiting for a free pooled connection

    # --- Retries ---
    max_retries: int = 3                # attempts AFTER the first try
    retry_backoff_base: float = 0.2     # exponential base (seconds)
    retry_backoff_max: float = 5.0      # cap per-wait
    retry_status_codes: tuple[int, ...] = (429, 502, 503, 504)

    http2: bool = False                 # flip on if downstreams support it

class PostgreSqlConfiguration(BaseModel):
    host: str 
    port: str 
    user: str 
    password: str 
    database: str

    # pool tuning
    pool_size: int = 10
    max_overflow: int = 5
    pool_recycle: int = 1800
    echo: bool = False 

    @property 
    def dsn(self) -> str:
        return f"postgresql+asyncpg://{self.user}:{self.password}@{self.host}:{self.port}/{self.database}"

class Settings(BaseSettings):
    app_name: str
    app_version: str
    environment: str = "local"
    langfuse_enabled: bool = False
    aws: AwsConfig
    services: dict[str, HttpClientConfiguration]
    postgres: PostgreSqlConfiguration
    llm_configuration: LLMConfiguration = LLMConfiguration()

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        env_nested_delimiter="__",
        extra="ignore"
    )

