package org.kbase.ragindexer.workflow.activity.stub;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.kbase.ragindexer.workflow.activity.DocumentActivities;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_INDEXING)
@ConditionalOnProperty(name = "ragindexer.stub-mode", havingValue = "true", matchIfMissing = true)
public class DocumentActivitiesStub implements DocumentActivities {

    @Override
    public String fetchDocument(String documentId) {
        log.info("[stub] Fetching document {} from document service", documentId);
        return "raw-content-of-" + documentId;
    }

    @Override
    public String processDocument(String documentId, String rawDocument) {
        log.info("[stub] Processing document {} ({} chars)", documentId, rawDocument.length());
        simulateWork();
        return "processed-chunks-of-" + documentId;
    }

    @Override
    public String storeDocument(String documentId, String indexPayload) {
        log.info("[stub] Storing index payload for document {} into vector store", documentId);
        return "store://rag-index/" + documentId;
    }

    // stand-in for real processing latency so runs are observable in the Temporal UI
    private void simulateWork() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
