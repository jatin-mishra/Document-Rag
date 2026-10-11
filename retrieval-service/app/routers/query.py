

from fastapi import APIRouter

from app.clients.embedding.dto import EmbeddingResponse
from app.configuration.dependency import QueryServiceDep
from app.context.dtos.query import GetEmbeddingRequest, QueryRequest, QueryResponse

router = APIRouter(prefix="/query")

@router.post("")
async def ask_llm(request: QueryRequest, query_service: QueryServiceDep) -> QueryResponse:
    return await query_service.query(request.query)

@router.post("/embed")
async def embed_query(request: GetEmbeddingRequest, query_service: QueryServiceDep) -> EmbeddingResponse:
    return await query_service.get_embeddings(request)