import asyncio 
import random 
import httpx 

IDEMPOTENT_METHODS = frozenset({"GET", "POST", "PUT", "DELETE"})

class RetryTransport(httpx.AsyncBaseTransport):
    """Wraps a transport, retrying transient failures for idempotent requests."""

    def __init__(
            self,
            wrapped: httpx.AsyncBaseTransport,
            *,
            max_retries: int,
            backoff_base: float,
            backoff_max: float,
            retry_status_codes: tuple[int, ...]):
        self._wrapped = wrapped
        self._max_retries = max_retries
        self._backoff_base = backoff_base
        self._backoff_max = backoff_max
        self._retry_status_codes = set(retry_status_codes)

    async def handle_async_request(self, request: httpx.Request) -> httpx.Response:

        if request.method not in IDEMPOTENT_METHODS:
            return await self._wrapped.handle_async_request(request)

        last_exception : Exception | None = None
        for attempt in range(self._max_retries + 1):
            try:
                response = await self._wrapped.handle_async_request(request)
                if response.status_code not in self._retry_status_codes:
                    return response 
                await response.aclose() 
                delay = self._retry_after(response) or self._backoff(attempt)
            except (httpx.ConnectError, httpx.ConnectTimeout, httpx.ReadError, httpx.WriteTimeout, httpx.PoolTimeout, httpx.RemoteProtocolError) as exc:
                last_exception = exc
                if attempt == self._max_retries:
                    raise exc
                delay = self._backoff(attempt)

        if last_exception:
            raise last_exception

        raise RuntimeError("unreachable")


    def _backoff(self, attempt: int) -> float:
        raw = self._backoff_base * (2 ** attempt)
        return min(raw, self._backoff_max) * (0.5 + random.random() / 2)   


    @staticmethod
    def _retry_after(response: httpx.Response) -> float | None:
        value = response.headers.get("Retry-After")
        if value and value.isdigit():
            return float(value)
        return None
          
