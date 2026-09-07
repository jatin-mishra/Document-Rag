


from app.clients.embedding.dto import EmbeddingResponse
from app.clients.embedding.embedding_client import EmbeddingClient
from app.context.dtos.query import GetEmbeddingRequest, GetEmbeddingResponse, QueryResponse
from app.services.llm.llm_proxy import LLMProxy


class QueryService:

    def __init__(self, llm_proxy: LLMProxy, embedding_client: EmbeddingClient):
        self.llm_proxy = llm_proxy
        self.embedding_client = embedding_client

    async def query(self, message: str) -> QueryResponse:
        response = await self.llm_proxy.send_message(message)
        return QueryResponse(answer=response)

    async def get_embeddings(self, request: GetEmbeddingRequest) -> EmbeddingResponse:
        return await self.embedding_client.embed(request.model, request.query)

