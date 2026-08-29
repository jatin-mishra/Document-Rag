package org.kbase.ragindexer.context.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkKeywordStoreModel {
    private String id;
    private String documentId;
    private Integer chunkIndex;
    private String content;
}
