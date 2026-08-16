package org.kbase.ragindexer.api;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.workflow.DocumentIndexingWorkflow;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class IndexController {

    private final WorkflowClient workflowClient;

    @PostMapping("/{documentId}/index")
    public ResponseEntity<StartIndexingResponse> startIndexing(@PathVariable String documentId) {
        String normalizedId = documentId.strip();
        if (normalizedId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String workflowId = "doc-index-" + normalizedId;
        DocumentIndexingWorkflow workflow = workflowClient.newWorkflowStub(
                DocumentIndexingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TaskQueues.DOCUMENT_INDEXING)
                        .setWorkflowId(workflowId)
                        .build());
        try {
            WorkflowExecution execution = WorkflowClient.start(workflow::index, normalizedId);
            log.info("Started workflow {} run {}", workflowId, execution.getRunId());
            return ResponseEntity.accepted()
                    .body(new StartIndexingResponse(workflowId, execution.getRunId(), "STARTED"));
        } catch (WorkflowExecutionAlreadyStarted e) {
            log.info("Workflow {} already running (run {})", workflowId, e.getExecution().getRunId());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new StartIndexingResponse(workflowId, e.getExecution().getRunId(), "ALREADY_RUNNING"));
        }
    }
}
