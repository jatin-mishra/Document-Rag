


from app.context.dtos.query import QueryResponse
from app.services.llm.llm_proxy import LLMProxy


class QueryService:

    def __init__(self, llm_proxy: LLMProxy):
        self.llm_proxy = llm_proxy

    async def query(self, message: str) -> QueryResponse:
        response = await self.llm_proxy.send_message(message)
        return QueryResponse(answer=response)