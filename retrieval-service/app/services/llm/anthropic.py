from anthropic import AsyncAnthropic
from langfuse import get_client, observe
from app.configuration.llm_configuration import AnthropicLLMConfiguration
from app.services.llm.llm_proxy import LLMProvider


class AnthropicProvider(LLMProvider):

    def __init__(self, configuration: AnthropicLLMConfiguration):
        self.api_key = configuration.api_key
        self.default_model = configuration.default_model
        self.client = AsyncAnthropic(api_key=self.api_key)

    @observe(name="anthropic.messages.create", capture_input=False, as_type="generation")
    async def send(self, query: str) -> str:
        message = await self.client.messages.create(
            max_tokens=1024,
            messages=[{"role": "user", "content": query}],
            model=self.default_model,
        )

        usage = message.usage
        usage_details = {
            key: value
            for key, value in {
                "input": usage.input_tokens,
                "output": usage.output_tokens,
                "cache_read_input_tokens": usage.cache_read_input_tokens,
                "cache_creation_input_tokens": usage.cache_creation_input_tokens,
            }.items()
            if value is not None
        }

        text = next((block.text for block in message.content if block.type == "text"), "")
        get_client().update_current_generation(
            input=query,
            output=text,
            model=self.default_model,
            usage_details=usage_details,
        )
        return text
