import aioboto3 
from app.context.models.documents import Document
from app.configuration.settings import Settings


class S3Service:

    def __init__(self, settings: Settings):
        self.aws_config = settings.aws
        self.session = aioboto3.Session(
            aws_access_key_id=self.aws_config.access_key,
            aws_secret_access_key=self.aws_config.secret_key,
            region_name=self.aws_config.region
        )
        self.bucket = self.aws_config.s3.bucket 
        self.prefix = self.aws_config.s3.path
        self.endpoint = self.aws_config.url

    def _generate_key(self, file_name: str):
        return f"{self.prefix}/{file_name}"


    async def upload_file(self, filename: str, body: bytes, content_type: str | None = None) -> tuple[str, str]:
        final_path = self._generate_key(filename)
        async with self.session.client("s3", endpoint_url=self.endpoint) as s3:
            response = await s3.put_object(
                Bucket=self.bucket,
                Key=final_path,
                Body=body,
                ContentType=content_type or "application/octet-stream"
            )
        return final_path, response["ETag"].strip('"')


    async def generate_presigned_url(self, key: str, expires_in: int = 3600) -> str:
        async with self.session.client("s3") as s3:
            return await s3.generate_presigned_url(
                "get_object",
                Params={"Bucket": self.bucket, "Key": key},
                ExpiresIn=expires_in,
            )