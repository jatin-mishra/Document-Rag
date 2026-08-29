package org.kbase.ragindexer.clients.document;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDocumentResponse {
    private String documentId;
    private String content;
}
