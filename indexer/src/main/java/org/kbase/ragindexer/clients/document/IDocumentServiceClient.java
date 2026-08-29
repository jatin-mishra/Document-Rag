package org.kbase.ragindexer.clients.document;

public interface IDocumentServiceClient {
    GetPresignedDocumentResponse getPresignedDocument(String documentId);
}
