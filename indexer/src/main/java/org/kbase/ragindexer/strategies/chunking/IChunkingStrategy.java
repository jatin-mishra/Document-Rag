package org.kbase.ragindexer.strategies.chunking;

import org.kbase.ragindexer.context.ChunkBatch;

import java.util.List;

public interface IChunkingStrategy {
    List<ChunkBatch> chunk(List<String> content, String documentId);
}
