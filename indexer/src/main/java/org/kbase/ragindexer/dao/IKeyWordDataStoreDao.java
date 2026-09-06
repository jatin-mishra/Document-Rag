package org.kbase.ragindexer.dao;

import org.kbase.ragindexer.context.model.ChunkKeywordStoreModel;

public interface IKeyWordDataStoreDao {

    /**
     * Idempotent create-or-replace of a chunk keyword document, keyed by the
     * caller-supplied custom id ({@link ChunkKeywordStoreModel#getId()}).
     * If a document with that id exists it is fully replaced, otherwise it is
     * created. Safe to retry (e.g. from a Temporal activity).
     */
    void upsert(ChunkKeywordStoreModel document);
}
