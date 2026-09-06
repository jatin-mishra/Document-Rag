package org.kbase.ragindexer.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.kbase.ragindexer.dto.IndexingRequest;
import org.kbase.ragindexer.error.AppException;

@WorkflowInterface
public interface DocumentIndexingWorkflow {

    @WorkflowMethod
    void index(IndexingRequest request) throws AppException;
}
