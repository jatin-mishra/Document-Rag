package org.kbase.ragindexer.workflow.activity.embedding;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.kbase.ragindexer.workflow.activity.EmbeddingActivities;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_EMBEDDING)
public class InMemoryEmbeddingActivity implements EmbeddingActivities {

    @Override
    public void embedAggregateAndStore(String documentId, String bucket, List<String> pathList) {
        /*
         * get chunks
         * call embedding model per chunk
         * store in vector db
         *
         * but for now:
         * store in elastic search to support keyword search
         */


    }
}
