

from datetime import datetime
from app.context.models.base import Base

from pgvector.sqlalchemy import Vector
from sqlalchemy import func
from sqlalchemy.orm import Mapped, mapped_column

EMBEDDING_DIMENSION = 1024  # bge-m3


class DocumentChunk(Base):

    __tablename__ = "chunk_vector_store"

    id: Mapped[str] = mapped_column(primary_key=True)
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(server_default=func.now(), onupdate=func.now())
    created_by: Mapped[str]
    updated_by: Mapped[str]

    document_id: Mapped[str]
    chunk_index: Mapped[int]
    content: Mapped[str]
    embedding: Mapped[list[float]] = mapped_column(Vector(EMBEDDING_DIMENSION))
