from dataclasses import dataclass
from pydantic import BaseModel
from app.constants.providers import EmbeddingModel

@dataclass
class EmbeddingRequest(BaseModel):
    model: EmbeddingModel
    query: str 

@dataclass
class EmbeddingResponse(BaseModel):
    model: EmbeddingModel 
    embeddings: list[list[float]]
    total_duration: int 
    load_duration: int 
    prompt_eval_count: int