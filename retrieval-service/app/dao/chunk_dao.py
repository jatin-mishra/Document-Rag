

from app.context.models.document_chunk import DocumentChunk
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession


class DocumentChunkDAO:

    def __init__(self, session: AsyncSession):
        self.session = session

    async def query_chunks(self, embedding: list[float], top_k: int = 5) -> list[DocumentChunk]:
        result = await self.session.execute(
            select(DocumentChunk)
            .order_by(DocumentChunk.embedding.cosine_distance(embedding))
            .limit(top_k)
        )
        return list(result.scalars().all())
