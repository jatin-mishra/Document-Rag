-- Schema for the pgvector-backed chunk store.
-- Apply manually against the target database (see README/run commands); this is
-- reference DDL, not an auto-run migration.
--
-- Requires a pgvector-enabled PostgreSQL image (e.g. pgvector/pgvector:pg16).

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE chunk_vector_store (
    id           TEXT           PRIMARY KEY,   -- documentId + chunk index (matches keyword store id)
    document_id  TEXT           NOT NULL,
    chunk_index  INT            NOT NULL,
    content      TEXT           NOT NULL,
    embedding    vector(1024)   NOT NULL,      -- must match vector-store.embedding-dimension
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by   TEXT           NOT NULL DEFAULT current_user,
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_by   TEXT           NOT NULL DEFAULT current_user,
    UNIQUE (document_id, chunk_index)
);

-- Approximate nearest-neighbour index for cosine similarity search.
CREATE INDEX IF NOT EXISTS idx_chunk_vector_store_hnsw
    ON chunk_vector_store USING hnsw (embedding vector_cosine_ops);
