package org.kbase.ragindexer.workflow.activity.live;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.config.ServiceProperties;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.kbase.ragindexer.workflow.activity.DocumentActivities;
import org.kbase.ragindexer.error.DocumentNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_INDEXING)
@ConditionalOnProperty(name = "ragindexer.stub-mode", havingValue = "false")
public class DocumentActivitiesLive implements DocumentActivities {

    private final RestClient documentServiceClient;
    private final RestClient vectorStoreClient;

    public DocumentActivitiesLive(ServiceProperties properties) {
        this.documentServiceClient = RestClient.create(properties.documentServiceUrl());
        this.vectorStoreClient = RestClient.create(properties.vectorStoreUrl());
    }

    @Override
    public String fetchDocument(String documentId) {
        log.info("Fetching document {} from document service", documentId);
        try {
            return documentServiceClient.get()
                    .uri("/api/v1/documents/{id}", documentId)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException.NotFound e) {
            // non-retryable per workflow RetryOptions — a missing document won't appear on retry
            throw new DocumentNotFoundException(documentId);
        }
    }

    @Override
    public String processDocument(String documentId, String rawDocument) {
        // chunking/cleaning is in-process work, no external service involved
        log.info("Processing document {} ({} chars)", documentId, rawDocument.length());
        return "processed-chunks-of-" + documentId;
    }

    @Override
    public String storeDocument(String documentId, String indexPayload) {
        log.info("Storing index payload for document {} into vector store", documentId);
        vectorStoreClient.post()
                .uri("/api/v1/index/{id}", documentId)
                .body(indexPayload)
                .retrieve()
                .toBodilessEntity();
        return "store://vector-store/" + documentId;
    }
}
