from fastapi import APIRouter, Depends
import uuid
from fastapi import File, UploadFile
from app.services.s3_service import S3Service
from app.configuration.dependency import S3Dep
from app.context.dtos.documents import PresignedRequest, PresignedResponse



router = APIRouter(prefix="/document", tags=["documents"])


@router.get("/{id}")
async def dummy(id: int):
    return {
            "id" : id,
            "message" : "You've got it"
        }


@router.post("")
async def add_document(
    s3_service: S3Dep,
    document: UploadFile = File(...)):
    file_id, path, etag, sizeInBytes = await s3_service.upload_file(document)
    return {
        "id" : file_id,
        "path" : path,
        "etag" : etag,
        "size" : sizeInBytes
    }


@router.post("/presigned")
async def get_presigned_url(request: PresignedRequest, s3_service: S3Dep) -> PresignedResponse:
    url = await s3_service.generate_presigned_url(request.path)
    return PresignedResponse(url=url)
