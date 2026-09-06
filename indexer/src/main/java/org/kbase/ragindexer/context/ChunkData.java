package org.kbase.ragindexer.context;

import java.util.List;

public record ChunkData (String docId, int chunkIdx, List<String> data) {
    public String join(){
        return String.join("\n", data);
    }
}
