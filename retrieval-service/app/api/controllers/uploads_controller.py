import logging

from fastapi import HTTPException, UploadFile

from app.dtos.upload_dtos import PresignedUrlRequest, PresignedUrlResponse, UploadResponse
from app.errors.service_errors import UploadServiceError, UploadTooLargeError
from app.services import upload_service

logger = logging.getLogger(__name__)


def upload_file(file: UploadFile) -> UploadResponse:
    try:
        result = upload_service.upload(file.file, file.filename or "file", file.content_type)
    except UploadTooLargeError as exc:
        raise HTTPException(status_code=413, detail=str(exc)) from None
    except UploadServiceError:
        logger.exception("upload failed for filename=%s", file.filename)
        raise HTTPException(status_code=502, detail="storage backend unavailable") from None

    return UploadResponse(
        bucket=result.bucket,
        key=result.key,
        filename=result.filename,
        content_type=result.content_type,
        size_bytes=result.size_bytes,
    )


def create_presigned_upload_url(body: PresignedUrlRequest) -> PresignedUrlResponse:
    try:
        result = upload_service.create_presigned_upload(body.filename, body.content_type)
    except UploadServiceError:
        logger.exception("presign failed for filename=%s", body.filename)
        raise HTTPException(status_code=502, detail="storage backend unavailable") from None

    return PresignedUrlResponse(
        bucket=result.bucket,
        key=result.key,
        url=result.url,
        expires_in=result.expires_in,
    )
