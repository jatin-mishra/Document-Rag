from fastapi import APIRouter

from app.dtos.document_dtos import (
    DocumentResponse,
)

router = APIRouter(prefix="/documents", tags=["documents"])


@router.post("/dummy", response_model=DocumentResponse, status_code=201)
def dummy():
    return "hello"