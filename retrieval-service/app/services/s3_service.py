import aioboto3
from botocore.config import Config
from app.context.models.documents import Document
from app.configuration.settings import Settings
from app.clients.indexing.indexing_client import IndexingClient
from fastapi import UploadFile
import uuid

class S3Service:

    def __init__(self, settings: Settings, indexing: IndexingClient):
        self.aws_config = settings.aws
        self.session = aioboto3.Session(
            aws_access_key_id=self.aws_config.access_key,
            aws_secret_access_key=self.aws_config.secret_key,
            region_name=self.aws_config.region
        )
        self.bucket = self.aws_config.s3.bucket 
        self.prefix = self.aws_config.s3.path
        self.endpoint = self.aws_config.url
        self.indexing = indexing

    def _generate_key(self, file_name: str, extension: str):
        return f"{self.prefix}/{file_name}.{extension}"

    async def upload_file(self, document: UploadFile) -> tuple[str, str]:
        content = await document.read()
        file_id = str(uuid.uuid4())
        extension = "txt"
        final_path = self._generate_key(file_id, extension)
        async with self.session.client("s3", endpoint_url=self.endpoint) as s3:
            response = await s3.put_object(
                Bucket=self.bucket,
                Key=final_path,
                Body=content,
                ContentType="application/octet-stream"
            )
        indexingResponse = await self.indexing.indexDocument(file_id, len(content))
        return file_id, final_path, response["ETag"].strip('"'), len(content)


    async def generate_presigned_url(self, key: str, expires_in: int = 3600) -> str:
        config = Config(signature_version="s3v4")
        async with self.session.client("s3", endpoint_url=self.endpoint, config=config) as s3:
            return await s3.generate_presigned_url(
                "get_object",
                Params={"Bucket": self.bucket, "Key": key},
                ExpiresIn=expires_in,
            )