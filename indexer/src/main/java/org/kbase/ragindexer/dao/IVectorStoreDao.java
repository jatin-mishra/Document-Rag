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
     * Idempotent create-or-replace of a chunk row keyed by {@link ChunkVectorStoreModel#getId()}.
     * Safe to retry (e.g. from a Temporal activity) — an existing id is fully replaced.
     */
    @SqlUpdate("""
            INSERT INTO chunk_vector_store (id, document_id, chunk_index, content, embedding)
            VALUES (:id, :documentId, :chunkIndex, :content, :embedding)
            ON CONFLICT (id) DO UPDATE SET
                document_id = EXCLUDED.document_id,
                chunk_index = EXCLUDED.chunk_index,
                content     = EXCLUDED.content,
                embedding   = EXCLUDED.embedding
            """)
    void upsert(@BindBean ChunkVectorStoreModel model);
}
