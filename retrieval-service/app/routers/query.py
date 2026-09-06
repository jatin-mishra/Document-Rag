

from fastapi import APIRouter

from app.configuration.dependency import QueryServiceDep
from app.context.dtos.query import QueryRequest, QueryResponse

router = APIRouter(prefix="/query")

@router.post("")
async def ask_llm(request: QueryRequest, query_service: QueryServiceDep) -> QueryResponse:
    return await query_service.query(request.query)
