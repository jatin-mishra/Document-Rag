from pydantic import BaseModel


class UploadResponse(BaseModel):
    bucket: str
    key: str
    filename: str
    content_type: str | None
    size_bytes: int


class PresignedUrlRequest(BaseModel):
    filename: str
    content_type: str | None = None


class PresignedUrlResponse(BaseModel):
    bucket: str
    key: str
    url: str
    method: str = "PUT"
    expires_in: int
