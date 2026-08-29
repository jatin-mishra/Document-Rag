package org.kbase.ragindexer.strategies.chunking;

import org.kbase.ragindexer.context.ChunkBatch;

import java.util.List;

public interface IChunkingStrategy {
    List<ChunkBatch> chunk(List<String> content, String path, String documentId);

    default String buildAbsolutePath(String path, String documentId, Integer chunkIndex){
        return path + "/" + documentId + "_" + chunkIndex + ".txt";
    }
}
