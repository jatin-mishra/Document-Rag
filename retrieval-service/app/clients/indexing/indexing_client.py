import httpx 
from app.context.dtos.documents import IndexingResponse

class IndexingClient:
    def __init__(self, client: httpx.AsyncClient):
        self.client = client 

    async def indexDocument(self, document_id: str, file_size: int) -> IndexingResponse:
        request = self.client.build_request(
            "POST",
            "/api/v1/documents/index",
            json={
                "document_id": document_id, 
                "file_size_in_bytes": file_size
            },
        )
        response = await self.client.send(request)
        response.raise_for_status()
        return IndexingResponse.model_validate(response.json())