package org.kbase.ragindexer.workflow.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface EmbeddingActivities {

    String generateEmbeddings(String documentId, String processedDocument);
}
