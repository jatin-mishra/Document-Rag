from typing import Any
from sqlalchemy import cast, func, update
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.ext.asyncio import AsyncSession
from app.context.models.documents import Document
from app.constants.status import UploadStatus, IndexingStatus
import uuid


class DocumentDao:

    def __init__(self, session: AsyncSession):
        self.session = session

    async def create(self, doc: Document) -> Document:
        if doc.id is None:
            doc.id = str(uuid.uuid4())
        self.session.add(doc)
        await self.session.flush()
        return doc

    async def get(self, doc_id: str) -> Document | None:
        return await self.session.get(Document, doc_id)

    async def commit(self) -> None:
        await self.session.commit()

    async def update_upload_success(self, doc_id: str, etag: str, final_path: str, status: UploadStatus) -> None:
        await self.session.execute(
            update(Document)
            .where(Document.id == doc_id)
            .values(upload_status=status,etag=etag,path=final_path)
        )

    async def update_upload_status(self, doc_id: str, status: UploadStatus, failure_reason: str | None = None) -> None:
        values: dict[Any, Any] = {Document.upload_status: status}
        if failure_reason is not None:
            values[Document.doc_metadata] = self._append_error("upload", failure_reason)
        await self.session.execute(
            update(Document)
            .where(Document.id == doc_id)
            .values(values)
        )

    async def update_indexing_status(self, doc_id: str, status: IndexingStatus, failure_reason: str | None = None) -> None:
        values: dict[Any, Any] = {Document.indexing_status: status}
        if failure_reason is not None:
            values[Document.doc_metadata] = self._append_error("indexing", failure_reason)
        await self.session.execute(
            update(Document)
            .where(Document.id == doc_id)
            .values(values)
        )

    def _append_error(self, section: str, reason: str):
        """Append one entry to metadata.errors, keeping earlier entries and any other keys."""
        entry = [{"section": section, "reason": reason}]
        appended = cast(
            func.coalesce(Document.doc_metadata["errors"], cast([], JSONB)), JSONB
        ).op("||")(cast(entry, JSONB))
        return Document.doc_metadata.op("||")(func.jsonb_build_object("errors", appended))
