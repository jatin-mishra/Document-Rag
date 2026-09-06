package org.kbase.ragindexer.clients.document;

import io.micrometer.common.util.StringUtils;
import lombok.*;
import org.kbase.ragindexer.error.AppException;
import org.kbase.ragindexer.error.Error;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPresignedDocumentResponse {
    private String url;

    public void validate() throws AppException {
        if(StringUtils.isBlank(url)){
            throw Error.bad_request.builder().message(String.format("presigned url is blank")).build();
        }
    }
}
