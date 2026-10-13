import logging
import os

from langfuse import Langfuse
from langfuse.api.commons.errors import UnauthorizedError

log = logging.getLogger(__name__)


def start_tracing(enabled: bool, environment: str, release: str) -> Langfuse | None:
    if not enabled:
        log.info("Langfuse tracing disabled by config")
        return None

    # Keys and base URL are read by the SDK from LANGFUSE_* in os.environ.
    # Fail fast with a clear message instead of the SDK silently disabling tracing.
    missing = [k for k in ("LANGFUSE_PUBLIC_KEY", "LANGFUSE_SECRET_KEY") if not os.environ.get(k)]
    if missing:
        raise RuntimeError(f"Langfuse enabled but missing env vars: {', '.join(missing)}")

    client = Langfuse(
        environment=environment,  # "local" | "staging" | "production"
        release=release,  # ties a trace to a deploy
        flush_at=15,  # SDK default 512: caps spans at risk on a hard kill
        flush_interval=1.0,  # SDK default 5s: caps seconds at risk on a hard kill
    )
    try:
        client.auth_check()
    except UnauthorizedError as e:
        raise RuntimeError("Langfuse credentials rejected; refusing to start") from e
    except Exception:
        log.error("Langfuse unreachable at startup; traces may not export", exc_info=True)

    log.info("Langfuse tracing started env=%s release=%s", environment, release)
    return client


def stop_tracing(client: Langfuse | None) -> None:
    if client is None:
        return
    # Bounded flush of anything still buffered, then exporter teardown.
    client.shutdown()
    log.info("Langfuse tracing stopped")
