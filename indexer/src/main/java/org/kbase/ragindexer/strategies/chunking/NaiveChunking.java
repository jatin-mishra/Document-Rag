package org.kbase.ragindexer.strategies.chunking;

import org.kbase.ragindexer.context.ChunkBatch;
import org.kbase.ragindexer.context.ChunkData;

import java.util.ArrayList;
import java.util.List;

public class NaiveChunking implements IChunkingStrategy {

    int numberOfLinesPerChunk = 5;
    int numberOfChunksPerBatch = 2;

    @Override
    public List<ChunkBatch> chunk(List<String> content, String path, String documentId) {
        List<ChunkBatch> allChunkBatchList = new ArrayList<>();

        ChunkBatch chunkBatch = new ChunkBatch(new ArrayList<>());
        ChunkData chunk = new ChunkData(documentId, 0, new ArrayList<>(), buildAbsolutePath(path, documentId, 0));

        int chunkIdx = 0;

        int itr = 0;
        while(itr < content.size()){
            if(chunk.data().size() == numberOfLinesPerChunk){
                if(chunkBatch.allChunks().size() == numberOfChunksPerBatch){
                    allChunkBatchList.add(chunkBatch);
                    chunkBatch = new ChunkBatch(new ArrayList<>());
                }
                chunkBatch.allChunks().add(chunk);
                ++chunkIdx;
                chunk = new ChunkData(documentId, chunkIdx, new ArrayList<>(), buildAbsolutePath(path, documentId, chunkIdx));
            }
            chunk.data().add(content.get(itr));
            itr++;
        }

        if(chunkBatch.allChunks().size() < numberOfChunksPerBatch){
            chunkBatch.allChunks().add(chunk);
        } else {
            allChunkBatchList.add(chunkBatch);
            chunkBatch = new ChunkBatch(new ArrayList<>());
            chunkBatch.allChunks().add(chunk);
        }
        allChunkBatchList.add(chunkBatch);

        return allChunkBatchList;
    }
}
