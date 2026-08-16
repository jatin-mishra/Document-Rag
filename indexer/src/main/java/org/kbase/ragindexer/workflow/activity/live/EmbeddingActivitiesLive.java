package org.kbase.ragindexer.workflow.activity.live;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.config.ServiceProperties;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.kbase.ragindexer.workflow.activity.EmbeddingActivities;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_INDEXING)
@ConditionalOnProperty(name = "ragindexer.stub-mode", havingValue = "false")
public class EmbeddingActivitiesLive implements EmbeddingActivities {

    private final RestClient embeddingServiceClient;

    public EmbeddingActivitiesLive(ServiceProperties properties) {
        this.embeddingServiceClient = RestClient.create(properties.embeddingServiceUrl());
    }

    @Override
    public String generateEmbeddings(String documentId, String processedDocument) {
        log.info("Generating embeddings for document {}", documentId);
        return embeddingServiceClient.post()
                .uri("/api/v1/embeddings")
                .body(processedDocument)
                .retrieve()
                .body(String.class);
    }
}
