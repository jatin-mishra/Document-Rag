


import os

from anthropic import AsyncAnthropic
from app.configuration.llm_configuration import AnthropicLLMConfiguration
from app.services.llm.llm_proxy import LLMProvider


class AnthropicProvider(LLMProvider):

    def __init__(self, configuration: AnthropicLLMConfiguration):
        self.api_key = configuration.api_key
        self.default_model = configuration.default_model
        self.client = AsyncAnthropic(api_key=self.api_key)

    async def send(self, query: str) -> str:
        message = await self.client.messages.create(
            max_tokens=1024,
            messages=[
                {
                    "role" : "user",
                    "content" : query
                }
            ],
            model=self.default_model
        )
        for block in message.content:
            if block.type == "text":
                return block.text

        return "No Reply"