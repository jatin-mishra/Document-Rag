import httpx 
from app.clients.http.retry_transport import RetryTransport
from app.configuration.settings import HttpClientConfiguration


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