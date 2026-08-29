package org.kbase.ragindexer.clients.document;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DocumentServiceClientImpl implements IDocumentServiceClient {
    @Override
    public GetPresignedDocumentResponse getPresignedDocument(String documentId) {
        return null;
    }
}
