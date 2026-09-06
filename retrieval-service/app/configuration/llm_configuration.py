
import os

from app.constants.providers import LLMProviderType
from pydantic import BaseModel


class AnthropicLLMConfiguration(BaseModel):
    default_model: str = os.environ.get("ANTHROPIC_AGENT_MODEL")
    api_key: str = os.environ.get("ANTHROPIC_AGENT_API_KEY")


class LLMConfiguration(BaseModel):
    anthropic: AnthropicLLMConfiguration = AnthropicLLMConfiguration()

    def getProviderConfiguration(self, provider_type: LLMProviderType) -> AnthropicLLMConfiguration | None:
        if provider_type == LLMProviderType.ANTHROPIC:
            return self.anthropic 
        return None


