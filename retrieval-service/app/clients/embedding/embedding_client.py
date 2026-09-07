import httpx
from app.clients.embedding.dto import EmbeddingResponse
from app.constants.providers import EmbeddingModel

class EmbeddingClient:

    def __init__(self, client: httpx.AsyncClient):
        self.client = client 
        self.embed_path = "/api/embed"

    async def embed(self, model: EmbeddingModel, query: str) -> EmbeddingResponse:
        request = self.client.build_request(
            "POST",
            self.embed_path,
            json={
               "model": model.value,
               "input" : [query]
            }
        )
        response = await self.client.send(request)
        response.raise_for_status()
        return EmbeddingResponse.model_validate(response.json())