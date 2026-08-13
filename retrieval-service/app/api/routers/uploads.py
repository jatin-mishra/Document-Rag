from fastapi import APIRouter, UploadFile

from app.api.controllers import uploads_controller as controller
from app.dtos.upload_dtos import PresignedUrlRequest, PresignedUrlResponse, UploadResponse

router = APIRouter(prefix="/uploads", tags=["uploads"])


@router.post("", response_model=UploadResponse)
def upload_file(file: UploadFile) -> UploadResponse:
    return controller.upload_file(file)


@router.post("/presigned-url", response_model=PresignedUrlResponse)
def create_presigned_upload_url(body: PresignedUrlRequest) -> PresignedUrlResponse:
    return controller.create_presigned_upload_url(body)
