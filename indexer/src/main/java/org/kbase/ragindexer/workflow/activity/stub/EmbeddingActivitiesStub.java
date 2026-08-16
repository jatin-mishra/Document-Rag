package org.kbase.ragindexer.workflow.activity.stub;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.kbase.ragindexer.workflow.activity.EmbeddingActivities;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_INDEXING)
@ConditionalOnProperty(name = "ragindexer.stub-mode", havingValue = "true", matchIfMissing = true)
public class EmbeddingActivitiesStub implements EmbeddingActivities {

    @Override
    public String generateEmbeddings(String documentId, String processedDocument) {
        log.info("[stub] Generating embeddings for document {}", documentId);
        return "embeddings://dummy/" + documentId;
    }
}
