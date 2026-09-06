from enum import Enum
from datetime import datetime
from typing import Any
from sqlalchemy import func, text
from sqlalchemy.dialects.postgresql import JSONB
from app.context.models.base import Base
from sqlalchemy.orm import Mapped, mapped_column

class IndexingStatus(str, Enum):
    NOT_STARTED = "NOT_STARTED"
    TRIGGERED = "TRIGGERED"
    TRIGGER_FAILED = "TRIGGER_FAILED"

class UploadStatus(str, Enum):
    INITIATED = "INITIATED"
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"

class Document(Base):
    __tablename__ = "documents"

    id: Mapped[str] = mapped_column(primary_key=True)
    bucket: Mapped[str]
    path: Mapped[str]
    # unknown until the upload completes, so the draft row is written without it
    etag: Mapped[str | None] = mapped_column(default=None)
    size: Mapped[int]
    indexing_status: Mapped[str]
    upload_status: Mapped[str]
    # "metadata" is reserved on DeclarativeBase, so the attribute is named
    # differently from the column it maps to
    doc_metadata: Mapped[dict[str, Any]] = mapped_column(
        "metadata", JSONB, default=dict, server_default=text("'{}'::jsonb")
    )
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(server_default=func.now(), onupdate=func.now())




    