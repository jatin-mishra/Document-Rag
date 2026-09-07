from enum import Enum

from pydantic import BaseModel

# LLM models
class LLMProviderType(str, Enum):
    ANTHROPIC = "anthropic"


#  Embedding Models
class EmbeddingModel(str, Enum):
    BGE_M3 = "bge-m3"

class EmbeddingModelConfig(BaseModel):
    dimension: int 

# EMBEDDING_MODEL_CONFIGS: dict[EmbeddingModel, EmbeddingModelConfig] = {
#     EmbeddingModel.BGE_M3: EmbeddingModelConfig(1024)
# }