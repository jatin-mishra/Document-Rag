from typing import IO

from botocore.exceptions import BotoCoreError, ClientError

from app.core.s3 import get_s3_client
from app.errors.dao_errors import S3DaoError


def upload_fileobj(bucket: str, key: str, fileobj: IO[bytes], content_type: str | None) -> None:
    extra_args = {"ContentType": content_type} if content_type else {}
    try:
        get_s3_client().upload_fileobj(fileobj, bucket, key, ExtraArgs=extra_args)
    except (ClientError, BotoCoreError) as exc:
        raise S3DaoError(f"failed to upload {key!r} to bucket {bucket!r}") from exc


def generate_presigned_put_url(
    bucket: str, key: str, content_type: str | None, expires_in: int
) -> str:
    params = {"Bucket": bucket, "Key": key}
    if content_type:
        params["ContentType"] = content_type
    try:
        return get_s3_client().generate_presigned_url(
            "put_object", Params=params, ExpiresIn=expires_in
        )
    except (ClientError, BotoCoreError) as exc:
        raise S3DaoError(f"failed to presign {key!r} for bucket {bucket!r}") from exc
