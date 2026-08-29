package org.kbase.ragindexer.context;

import java.util.List;

public record ChunkData (String docId, int chunkIdx, List<String> data, String path) {
    public String join(){
        return String.join("\n", data);
    }
}
