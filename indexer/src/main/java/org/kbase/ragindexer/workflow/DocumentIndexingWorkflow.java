package org.kbase.ragindexer.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DocumentIndexingWorkflow {

    @WorkflowMethod
    String index(String documentId);
}
