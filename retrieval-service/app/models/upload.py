from dataclasses import dataclass


@dataclass
class UploadResult:
    bucket: str
    key: str
    filename: str
    content_type: str | None
    size_bytes: int


@dataclass
class PresignedUploadResult:
    bucket: str
    key: str
    url: str
    expires_in: int
