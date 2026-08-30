from enum import Enum


class UploadStatus(str, Enum):
    INITIATED = "INITIATED"
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"

class Document:
    id: str 
    bucket: str 
    path: str 
    etag: str 
    size: int 
    upload_status: UploadStatus 
    