
import logging

from app.dao.document_dao import DocumentDao
from app.context.models.documents import Document 
from app.constants.status import UploadStatus, IndexingStatus
from app.clients.indexing.indexing_client import IndexingClient
from app.services.s3_service import S3Service
from fastapi import UploadFile
import uuid 
from app.context.dtos.documents import AddDocumentResponse, PresignedResponse

logger = logging.getLogger(__name__)

class DocumentService:

    def __init__(self, 
                 document_dao: DocumentDao, 
                 s3_service: S3Service,
                 indexing_client: IndexingClient):
        self.dao = document_dao
        self.s3 = s3_service
        self.indexing_client = indexing_client

    async def _read_content(self, document: UploadFile):
        return await document.read()

    @staticmethod
    def _failure_reason(exc: Exception, limit: int = 500) -> str:
        # the type matters as much as the message: some client errors carry an empty one
        return f"{type(exc).__name__}: {exc}"[:limit]

    async def add_document(self, document: UploadFile) -> AddDocumentResponse:

        # generate id and read content
        file_id = str(uuid.uuid4())
        content = await self._read_content(document)
        extension = "txt"

        # prepare draft version
        initial_document = Document(
            id = file_id,
            bucket = self.s3.getBucket(),
            path = self.s3.getPrefixPath(),
            size = len(content),
            upload_status = UploadStatus.INITIATED,
            indexing_status = IndexingStatus.NOT_STARTED,
        )

        # commit the audit row on its own so it survives any later failure
        record = await self.dao.create(initial_document)
        await self.dao.commit()

        # upload to s3 and mark status
        try:
            s3_response = await self.s3.upload_file(record.id, extension, content)
        except Exception as exc:
            logger.exception("s3 upload failed for document %s", record.id)
            await self.dao.update_upload_status(record.id, UploadStatus.FAILED, self._failure_reason(exc))
            await self.dao.commit()
            raise

        await self.dao.update_upload_success(record.id, s3_response.etag, s3_response.final_path, UploadStatus.SUCCESS)
        await self.dao.commit()

        # the file is already stored, so a failed trigger must not fail the request
        try:
            await self.indexing_client.indexDocument(record.id, len(content))
            indexing_status = IndexingStatus.TRIGGERED
            failure_reason = None
        except Exception as exc:
            logger.exception("indexing trigger failed for document %s", record.id)
            indexing_status = IndexingStatus.TRIGGER_FAILED
            failure_reason = self._failure_reason(exc)

        await self.dao.update_indexing_status(record.id, indexing_status, failure_reason)
        await self.dao.commit()

        return AddDocumentResponse(
            file_id=s3_response.file_id,
            final_path=s3_response.final_path,
            etag=s3_response.etag,
            file_size=s3_response.file_size,
            status=UploadStatus.SUCCESS,
            indexing_status=indexing_status,
        )

    async def generate_presigned_url(self, document_id) -> PresignedResponse:
        document = await self.dao.get(doc_id=document_id)
        return await self.s3.generate_presigned_url(document.path)

