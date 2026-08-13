import io
from typing import IO

from app.core.config import get_settings
from app.daos import s3_dao
from app.errors.dao_errors import S3DaoError
from app.errors.service_errors import UploadServiceError, UploadTooLargeError
from app.models.upload import PresignedUploadResult, UploadResult
from app.utils.file_utils import build_object_key


def upload(fileobj: IO[bytes], filename: str, content_type: str | None) -> UploadResult:
    settings = get_settings()
    max_bytes = settings.s3_upload_max_bytes

    buffer = io.BytesIO()
    size = 0
    while chunk := fileobj.read(1024 * 1024):
        size += len(chunk)
        if size > max_bytes:
            raise UploadTooLargeError(f"file exceeds max upload size of {max_bytes} bytes")
        buffer.write(chunk)
    buffer.seek(0)

    key = build_object_key(filename)
    try:
        s3_dao.upload_fileobj(settings.s3_bucket_name, key, buffer, content_type)
    except S3DaoError as exc:
        raise UploadServiceError(f"failed to store {filename!r}") from exc

    return UploadResult(
        bucket=settings.s3_bucket_name,
        key=key,
        filename=filename,
        content_type=content_type,
        size_bytes=size,
    )


def create_presigned_upload(filename: str, content_type: str | None) -> PresignedUploadResult:
    settings = get_settings()
    key = build_object_key(filename)
    try:
        url = s3_dao.generate_presigned_put_url(
            settings.s3_bucket_name,
            key,
            content_type,
            settings.s3_presigned_url_expiry_seconds,
        )
    except S3DaoError as exc:
        raise UploadServiceError(f"failed to presign {filename!r}") from exc

    return PresignedUploadResult(
        bucket=settings.s3_bucket_name,
        key=key,
        url=url,
        expires_in=settings.s3_presigned_url_expiry_seconds,
    )
