


from app.clients.embedding.dto import EmbeddingResponse
from app.clients.embedding.embedding_client import EmbeddingClient
from app.constants.providers import EmbeddingModel
from app.context.dtos.query import GetEmbeddingRequest, GetEmbeddingResponse, QueryResponse
from app.dao.chunk_dao import DocumentChunkDAO
from app.prompts import render_rag_prompt
from app.services.llm.llm_proxy import LLMProxy


class QueryService:

    def __init__(self, 
                 llm_proxy: LLMProxy, 
                 embedding_client: EmbeddingClient, 
                 chunk_dao: DocumentChunkDAO):
        self.llm_proxy = llm_proxy
        self.embedding_client = embedding_client
        self.chunk_dao = chunk_dao

    async def query(self, message: str) -> QueryResponse:
        embedding = await self.embedding_client.embed(EmbeddingModel.BGE_M3, message)
        chunks = await self.chunk_dao.query_chunks(embedding.embeddings[0], 5)
        prompt = render_rag_prompt(query=message, context=[chunk.content for chunk in chunks])
        answer = await self.llm_proxy.send_message(prompt)
        return QueryResponse(answer=answer)

    async def get_embeddings(self, request: GetEmbeddingRequest) -> EmbeddingResponse:
        return await self.embedding_client.embed(request.model, request.query)

