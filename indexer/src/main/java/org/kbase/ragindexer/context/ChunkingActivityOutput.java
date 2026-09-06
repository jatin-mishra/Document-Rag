package org.kbase.ragindexer.context;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkingActivityOutput {
    private String documentId;
    private String bucket;
    private List<ChunkBatch> chunkBatchList;
}
