

from typing import Annotated

from fastapi import APIRouter, Header

from app.clients.embedding.dto import EmbeddingResponse
from app.configuration.dependency import QueryServiceDep
from app.context.dtos.query import GetEmbeddingRequest, QueryRequest, QueryResponse

router = APIRouter(prefix="/query")

@router.post("/session/{session_id}/ask")
async def ask_llm(
    session_id: str, 
    request: QueryRequest, 
    query_service: QueryServiceDep,
    user_id: str = Header(..., alias="X-User-ID")) -> QueryResponse:
    request.session_id = session_id
    request.user_id = user_id
    return await query_service.query(request)

@router.post("/embed")
async def embed_query(request: GetEmbeddingRequest, query_service: QueryServiceDep) -> EmbeddingResponse:
    return await query_service.get_embeddings(request)