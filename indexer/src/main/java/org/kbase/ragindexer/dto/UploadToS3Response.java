package org.kbase.ragindexer.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadToS3Response {
    private String bucketId;
    private String path;
    private long fileSize;
}