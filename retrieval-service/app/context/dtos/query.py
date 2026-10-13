


from pydantic import BaseModel

from app.constants.providers import EmbeddingModel


class QueryRequest(BaseModel):
    query: str 
    user_id: str | None = None 
    session_id: str | None = None 

class QueryResponse(BaseModel):
    answer : str 


class GetEmbeddingRequest(BaseModel):
    model: EmbeddingModel 
    query: str 
