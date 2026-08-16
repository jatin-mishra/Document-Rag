package org.kbase.ragindexer.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kbase.ragindexer.workflow.activity.DocumentActivities;
import org.kbase.ragindexer.workflow.activity.stub.DocumentActivitiesStub;
import org.kbase.ragindexer.workflow.activity.stub.EmbeddingActivitiesStub;

class DocumentIndexingWorkflowTest {

    @RegisterExtension
    static final TestWorkflowExtension testWorkflow = TestWorkflowExtension.newBuilder()
            .registerWorkflowImplementationTypes(DocumentIndexingWorkflowImpl.class)
            .setDoNotStart(true)
            .build();

    @Test
    void indexesDocumentThroughFetchProcessStore(
            TestWorkflowEnvironment env, Worker worker, DocumentIndexingWorkflow workflow) {
        worker.registerActivitiesImplementations(new DocumentActivitiesStub(), new EmbeddingActivitiesStub());
        env.start();

        String result = workflow.index("DOC-1");

        assertThat(result).isEqualTo("store://rag-index/DOC-1");
    }

    @Test
    void retriesTransientActivityFailureUntilSuccess(
            TestWorkflowEnvironment env, Worker worker, DocumentIndexingWorkflow workflow) {
        AtomicInteger fetchAttempts = new AtomicInteger();
        worker.registerActivitiesImplementations(new EmbeddingActivitiesStub(), new DocumentActivities() {
            @Override
            public String fetchDocument(String documentId) {
                if (fetchAttempts.incrementAndGet() <= 2) {
                    throw new RuntimeException("simulated transient document-service outage");
                }
                return "raw-content-of-" + documentId;
            }

            @Override
            public String processDocument(String documentId, String rawDocument) {
                return "processed-chunks-of-" + documentId;
            }

            @Override
            public String storeDocument(String documentId, String indexPayload) {
                return "store://rag-index/" + documentId;
            }
        });
        env.start();

        String result = workflow.index("DOC-2");

        assertThat(result).isEqualTo("store://rag-index/DOC-2");
        assertThat(fetchAttempts.get()).isEqualTo(3);
    }
}
