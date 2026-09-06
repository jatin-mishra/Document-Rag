package org.kbase.ragindexer.workflow.activity;

import io.temporal.activity.ActivityInterface;

import java.util.List;

@ActivityInterface
public interface EmbeddingActivities {
    void embedAggregateAndStore(String documentId, List<Integer> chunkIds);
}
