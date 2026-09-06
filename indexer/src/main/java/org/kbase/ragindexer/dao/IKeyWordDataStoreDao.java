package org.kbase.ragindexer.dao;

import org.kbase.ragindexer.context.model.ChunkKeywordStoreModel;

import java.util.List;

public interface IKeyWordDataStoreDao {

    /**
     * Idempotent create-or-replace of a chunk keyword document, keyed by the
     * caller-supplied custom id ({@link ChunkKeywordStoreModel#getId()}).
     * If a document with that id exists it is fully replaced, otherwise it is
     * created. Safe to retry (e.g. from a Temporal activity).
     */
    void upsert(ChunkKeywordStoreModel document);

    /**
     * Fetches the chunk keyword documents for the given custom ids in a single
     * round-trip. Blank/duplicate ids are ignored and missing documents are
     * skipped, so the returned list may be smaller than {@code ids} and its
     * order is not guaranteed to match the input.
     */
    List<ChunkKeywordStoreModel> batchGet(List<String> ids);
}
