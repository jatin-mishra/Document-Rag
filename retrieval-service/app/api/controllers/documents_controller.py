import logging

from fastapi import HTTPException, UploadFile

from app.dtos.document_dtos import (
    CompleteUploadRequest,
    DocumentResponse,
    PresignedUploadRequest,
    PresignedUploadResponse,
)
from app.errors.service_errors import (
    DocumentConflictError,
    DocumentInvalidFileError,
    DocumentInvalidRequestError,
    DocumentNotFoundError,
    DocumentServiceError,
    DocumentUploadIncompleteError,
    DocumentUploadTooLargeError,
)
from app.models.document import ChunkingStrategy
from app.models.upload import (
    CompletedPart,
    CompleteUploadContext,
    DirectUploadContext,
    PresignedUploadContext,
)
from app.mappers.document_mapper import _to_presigned_response, _to_response
from app.services import document_service

logger = logging.getLogger(__name__)


def add_document(
    file: UploadFile,
    chunking_strategy: ChunkingStrategy,
    is_context_embedding_enabled: bool,
    created_by: str | None,
) -> DocumentResponse:
    ctx = DirectUploadContext(
        file_name=file.filename or "",
        content=file.file.read(),
        chunking_strategy=chunking_strategy,
        is_context_embedding_enabled=is_context_embedding_enabled,
        created_by=created_by,
    )
    try:
        document = document_service.add_document(ctx)
    except DocumentInvalidFileError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from None
    except DocumentUploadTooLargeError as exc:
        raise HTTPException(status_code=413, detail=str(exc)) from None
    except DocumentConflictError as exc:
        raise HTTPException(status_code=409, detail=str(exc)) from None
    except DocumentServiceError:
        logger.exception("failed to add document filename=%s", file.filename)
        raise HTTPException(status_code=502, detail="storage/database unavailable") from None
    return _to_response(document)


def create_presigned_upload(body: PresignedUploadRequest) -> PresignedUploadResponse:
    ctx = PresignedUploadContext(
        file_name=body.file_name,
        content_type=body.content_type,
        size_bytes=body.size_bytes,
        chunking_strategy=body.chunking_strategy,
        is_context_embedding_enabled=body.is_context_embedding_enabled,
        created_by=body.created_by,
    )
    try:
        result = document_service.create_presigned_upload(ctx)
    except DocumentInvalidFileError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from None
    except DocumentInvalidRequestError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from None
    except DocumentConflictError as exc:
        raise HTTPException(status_code=409, detail=str(exc)) from None
    except DocumentServiceError:
        logger.exception("failed to create presigned upload file_name=%s", body.file_name)
        raise HTTPException(status_code=502, detail="storage/database unavailable") from None
    return _to_presigned_response(result)


def complete_upload(body: CompleteUploadRequest) -> DocumentResponse:
    ctx = CompleteUploadContext(
        document_id=body.document_id,
        upload_id=body.upload_id,
        parts=(
            [CompletedPart(part_number=part.part_number, etag=part.etag) for part in body.parts]
            if body.parts
            else None
        ),
    )
    try:
        document = document_service.complete_upload(ctx)
    except DocumentNotFoundError:
        raise HTTPException(status_code=404, detail="document not found") from None
    except DocumentInvalidRequestError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from None
    except DocumentUploadIncompleteError as exc:
        raise HTTPException(status_code=409, detail=str(exc)) from None
    except DocumentConflictError as exc:
        raise HTTPException(status_code=409, detail=str(exc)) from None
    except DocumentServiceError:
        logger.exception("failed to complete upload document_id=%s", body.document_id)
        raise HTTPException(status_code=502, detail="storage/database unavailable") from None
    return _to_response(document)
