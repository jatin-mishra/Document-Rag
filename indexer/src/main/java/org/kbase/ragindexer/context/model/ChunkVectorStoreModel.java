package org.kbase.ragindexer.context.model;

import com.pgvector.PGvector;
import lombok.*;

/**
 * One row of the {@code chunk_vector_store} table: a document chunk plus its
 * embedding. Keyed by {@link #id} (documentId + chunk index), mirroring
 * {@link ChunkKeywordStoreModel} so the same chunk is addressable in both the
 * keyword (Elasticsearch) and vector (PostgreSQL/pgvector) stores.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkVectorStoreModel {
    private String id;
    private String documentId;
    private Integer chunkIndex;
    private String content;
    private PGvector embedding;
}
