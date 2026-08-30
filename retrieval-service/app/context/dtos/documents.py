from pydantic import BaseModel

class PresignedRequest(BaseModel):
    path: str 


class PresignedResponse(BaseModel):
    url: str 