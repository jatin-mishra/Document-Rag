package org.kbase.ragindexer.workflow.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface DocumentActivities {

    String fetchDocument(String documentId);

    String processDocument(String documentId, String rawDocument);

    String storeDocument(String documentId, String indexPayload);
}
