

from abc import ABC, abstractmethod

from app.configuration.llm_configuration import LLMConfiguration
from app.constants.providers import LLMProviderType

class LLMProvider(ABC):
    @abstractmethod
    async def send(self, query: str) -> str:
        pass


class LLMProxy:

    def __init__(self, anthropic_provider: LLMProvider):
        self.anthropic = anthropic_provider
        self.default_provider = LLMProviderType.ANTHROPIC
        self.providers = {
            LLMProviderType.ANTHROPIC: self._send_to_anthropic
        }

    async def send_message(self, message: str, provider_type: LLMProviderType | None = None) -> str:
        provider = self.providers.get(provider_type or self.default_provider)
        if provider is None:
            raise ValueError(f"unknown provider: {provider_type}")
        return await provider(message)

    async def _send_to_anthropic(self, message: str) -> str:
        return await self.anthropic.send(message)
