


from pydantic import BaseModel

from app.constants.providers import EmbeddingModel


class QueryRequest(BaseModel):
    query: str 


class QueryResponse(BaseModel):
    answer : str 


class GetEmbeddingRequest(BaseModel):
    model: EmbeddingModel 
    query: str 

class GetEmbeddingResponse(BaseModel):
    response: list[list[float]]