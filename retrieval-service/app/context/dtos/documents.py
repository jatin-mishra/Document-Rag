from pydantic import BaseModel

class PresignedResponse(BaseModel):
    url: str 

class IndexingResponse(BaseModel):
    workflow_id: str 
    run_id: str 
    status: str 

class S3UploadResponse(BaseModel):
    file_id: str 
    final_path: str
    etag: str 
    file_size: int 
    bucket: str

class AddDocumentResponse(BaseModel):
    file_id: str 
    final_path: str
    etag: str 
    file_size: int 
    status: str
    indexing_status: str