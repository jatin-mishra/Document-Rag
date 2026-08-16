package org.kbase.ragindexer.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.kbase.ragindexer.workflow.activity.DocumentActivities;
import org.kbase.ragindexer.error.DocumentNotFoundException;
import org.kbase.ragindexer.workflow.activity.EmbeddingActivities;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = TaskQueues.DOCUMENT_INDEXING)
public class DocumentIndexingWorkflowImpl implements DocumentIndexingWorkflow {

    private static final Logger log = Workflow.getLogger(DocumentIndexingWorkflowImpl.class);

    private final DocumentActivities activities = Workflow.newActivityStub(
            DocumentActivities.class,
            ActivityOptions.newBuilder()
                    // max time a single activity attempt may run before Temporal times it out and retries
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    // total budget across all attempts of one activity, including retry wait times
                    .setScheduleToCloseTimeout(Duration.ofMinutes(5))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(2))
                            .setBackoffCoefficient(2.0)
                            .setMaximumInterval(Duration.ofSeconds(60))
                            .setMaximumAttempts(5)
                            // permanent failures must fail fast, not burn retries
                            .setDoNotRetry(DocumentNotFoundException.class.getName())
                            .build())
                    .build());

    // separate stub, separate profile: embedding batches are slow and the API is
    // rate-limited, so longer timeouts and gentler backoff than the document activities
    private final EmbeddingActivities embeddingActivities = Workflow.newActivityStub(
            EmbeddingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .setScheduleToCloseTimeout(Duration.ofMinutes(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(5))
                            .setBackoffCoefficient(2.0)
                            .setMaximumInterval(Duration.ofMinutes(2))
                            .setMaximumAttempts(6)
                            .build())
                    .build());

    @Override
    public String index(String documentId) {
        log.info("Indexing started for document {}", documentId);
        String rawDocument = activities.fetchDocument(documentId);
        String processedDocument = activities.processDocument(documentId, rawDocument);
        String embeddingsRef = embeddingActivities.generateEmbeddings(documentId, processedDocument);
        String storageRef = activities.storeDocument(documentId, embeddingsRef);
        log.info("Indexing completed for document {} -> {}", documentId, storageRef);
        return storageRef;
    }
}
