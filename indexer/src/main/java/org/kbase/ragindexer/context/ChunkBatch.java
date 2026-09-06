package org.kbase.ragindexer.context;

import java.util.List;

public record ChunkBatch (List<ChunkData> allChunks) {
    public List<Integer> chunkIds() {
        return allChunks.stream().map(ChunkData::chunkIdx).toList();
    }
}
