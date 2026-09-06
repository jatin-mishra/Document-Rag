package org.kbase.ragindexer.dao;

import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kbase.ragindexer.context.model.ChunkVectorStoreModel;

/**
 * JDBI SqlObject DAO for the pgvector-backed chunk store. The interface itself
 * is the whole implementation — JDBI generates the binding/execution from the
 * declared SQL. Contrast with {@link IKeyWordDataStoreDao}, whose Elasticsearch
 * impl is hand-written.
 */
public interface IVectorStoreDao {

    /**
     * Idempotent create-or-update of a chunk row keyed by {@link ChunkVectorStoreModel#getId()}.
     * Safe to retry (e.g. a Temporal activity re-running after a mid-batch failure) and safe to
     * re-index an already-indexed document: an existing id has its content/embedding refreshed
     * rather than raising a unique violation. Provenance columns (created_at, created_by) are
     * preserved from the original insert.
     *
     * <p>The table also carries UNIQUE (document_id, chunk_index), but id is derived as
     * {@code documentId + "_" + chunkIndex}, so a conflict on that pair always implies a
     * conflict on the primary key — targeting {@code (id)} alone is sufficient.
     */
    @SqlUpdate("""
            INSERT INTO chunk_vector_store (id, document_id, chunk_index, content, embedding, created_at, created_by, updated_at, updated_by)
            VALUES (:id, :documentId, :chunkIndex, :content, :embedding, :createdAt, :createdBy, :updatedAt, :updatedBy)
            ON CONFLICT (id) DO UPDATE SET
                document_id = EXCLUDED.document_id,
                chunk_index = EXCLUDED.chunk_index,
                content     = EXCLUDED.content,
                embedding   = EXCLUDED.embedding,
                updated_at  = EXCLUDED.updated_at,
                updated_by  = EXCLUDED.updated_by
            """)
    void upsert(@BindBean ChunkVectorStoreModel model);
}
