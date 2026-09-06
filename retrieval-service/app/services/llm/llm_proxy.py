

from abc import ABC, abstractmethod

from app.configuration.llm_configuration import LLMConfiguration
from app.constants.providers import LLMProviderType

class LLMProvider(ABC):
    @abstractmethod
    async def send(query: str) -> str:
        pass


class LLMProxy(ABC):

    def __init__(self, anthropic_provider: LLMProvider):
        self.anthropic = anthropic_provider
        self.default_provider = LLMProviderType.ANTHROPIC
        self.providers = {
            LLMProviderType.ANTHROPIC: self._send_to_anthropic
        }

    async def send_message(self, provider_type: LLMProviderType, message: str) -> str:
        provider = self.providers.get(provider_type)
        if provider is None:
            raise ValueError("wrong provider specified")
        return await provider(message)

    async def send_message(self, message: str) -> str:
        return await self.providers.get(self.default_provider)(message)

    async def _send_to_anthropic(self, message: str) -> str:
        return await self.anthropic.send(message)





