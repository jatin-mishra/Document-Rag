package org.kbase.ragindexer.context.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkKeywordStoreModel extends BaseModel {
    private String documentId;
    private Integer chunkIndex;
    private String content;
}
