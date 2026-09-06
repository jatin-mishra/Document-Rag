from fastapi import APIRouter
from fastapi import File, UploadFile
from app.configuration.dependency import DocumentServiceDep
from app.context.dtos.documents import PresignedResponse



router = APIRouter(prefix="/document", tags=["documents"])


@router.post("")
async def add_document(
    document_service: DocumentServiceDep, # type: ignore
    document: UploadFile = File(...)):
    return await document_service.add_document(document)


@router.post("/{document_id}/presigned")
async def get_presigned_url(
    document_id: str,
    document_service: DocumentServiceDep) -> PresignedResponse:
    url = await document_service.generate_presigned_url(document_id)
    return PresignedResponse(url=url)
