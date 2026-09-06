package org.kbase.ragindexer.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.kbase.ragindexer.context.ChunkingActivityOutput;
import org.kbase.ragindexer.dto.IndexingRequest;
import org.kbase.ragindexer.error.AppException;
import org.kbase.ragindexer.workflow.activity.ChunkingActivities;
import org.kbase.ragindexer.workflow.activity.EmbeddingActivities;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@WorkflowImpl(taskQueues = TaskQueues.DOCUMENT_INDEXING)
public class DocumentIndexingWorkflowImpl implements DocumentIndexingWorkflow {

    private static final Logger log = Workflow.getLogger(DocumentIndexingWorkflowImpl.class);

    private final ChunkingActivities chunkingActivities;
    private final EmbeddingActivities embeddingActivities;

    @Autowired
    public DocumentIndexingWorkflowImpl(
            @Qualifier("chunking") ActivityOptions chunkingOptions,
            @Qualifier("embedding") ActivityOptions embeddingOptions){
        chunkingActivities = Workflow.newActivityStub(ChunkingActivities.class, chunkingOptions);
        embeddingActivities = Workflow.newActivityStub(EmbeddingActivities.class, embeddingOptions);
        log.info("dependencies injected. activities created.");
    }

    @Override
    public void index(IndexingRequest request) throws AppException {
        log.info("Chunking started for document {}", request.getDocumentId().strip());
        ChunkingActivityOutput output = chunkingActivities.fetchChunkAndStore(request);
        log.info("Embedding started for document {}", request.getDocumentId().strip());
        // do parallel processing
//        for(ChunkBatch batch : output.getChunkBatchList()){
//            embeddingActivities.embedAggregateAndStore(
//                    request.documentId().strip(),
//                    output.getBucket(),
//                    batch.allChunks().stream().map(ChunkData::path).toList());
//        }
        log.info("Indexing completed for document {}", request.getDocumentId().strip());
    }
}
