from fastapi import APIRouter, Depends
import uuid
from fastapi import File, UploadFile
from app.services.s3_service import S3Service
from app.configuration.dependency import get_s3_service
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
    document: UploadFile = File(...),
    s3_service: S3Service = Depends(get_s3_service)):
    content = await document.read()
    file_name = f"{str(uuid.uuid4())}.txt"
    path, etag = await s3_service.upload_file(file_name, content)
    return {
        "id" : file_name.split('.')[0],
        "path" : path,
        "etag" : etag,
        "content" : content.decode("utf-8"),
        "size" : len(content)
    }

@router.post("/presigned")
async def get_presigned_url(request: PresignedRequest, s3_service: S3Service = Depends(get_s3_service)) -> PresignedResponse:
    url = await s3_service.generate_presigned_url(request.path)
    return PresignedResponse(url=url)
