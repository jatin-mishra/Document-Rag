package org.kbase.ragindexer.clients.document;

import io.micrometer.common.util.StringUtils;
import lombok.*;
import org.kbase.ragindexer.constants.ChunkingStrategy;
import org.kbase.ragindexer.error.AppException;
import org.kbase.ragindexer.error.Error;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPresignedDocumentResponse {
    private String documentId;
    private ChunkingStrategy chunkingStrategy;
    private PresignedUrlDetail details;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PresignedUrlDetail {
        private String url;
        private String method;
        private String contentType;
        private Timestamp expiresAt;
    }

    public void validate() throws AppException {
        if(Objects.isNull(details)){
            throw Error.bad_request.builder().message(String.format("Details are null, documentId: %s", documentId)).build();
        }
        if(Objects.isNull(details.getUrl())){
            throw Error.bad_request.builder().message(String.format("Presigned url is invalid, documentId: %s", documentId)).build();
        }
        if(Objects.isNull(details.getExpiresAt()) || details.getExpiresAt().before(Timestamp.from(Instant.now()))){
            throw Error.bad_request.builder().message(String.format("invalid expiry or already expired, documentId: %s", documentId)).build();
        }
    }
}
