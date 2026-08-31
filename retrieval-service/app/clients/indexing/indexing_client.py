import httpx 

class IndexingClient:
    def __init__(self, client: httpx.AsyncClient):
        self.client = client 

    async def indexDocument(self, document_id: str, file_size: int):
        request = self.client.build_request(
            "POST",
            "/api/v1/documents/index",
            json={"documentId": document_id, "fileSizeInBytes": file_size},
        )
        print()
        print()
        print()
        print()
        print(f"{request.method} {request.url}")
        for name, value in request.headers.items():
            print(f"{name}: {value}")
        print()
        print(request.content.decode())
        print()
        print()

        response = await self.client.send(request)
        response.raise_for_status()
        return response.json()