package org.kbase.ragindexer.controller;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.dto.IndexingRequest;
import org.kbase.ragindexer.dto.IndexingResponse;
import org.kbase.ragindexer.workflow.DocumentIndexingWorkflow;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.annotations.NotNull;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class IndexController {

    private final WorkflowClient workflowClient;


    @SneakyThrows
    @PostMapping("/index")
    public ResponseEntity<IndexingResponse> startIndexing(
            @RequestBody @NotNull IndexingRequest request
    ) {
        log.info("Received indexing request for document: {}", request.toString());
        String workflowId = "doc-index-" + request.getDocumentId().strip();
        DocumentIndexingWorkflow workflow = workflowClient.newWorkflowStub(
                DocumentIndexingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TaskQueues.DOCUMENT_INDEXING)
                        .setWorkflowId(workflowId)
                        .build());
        try {
            WorkflowExecution execution = WorkflowClient.start(workflow::index, request);
            log.info("Started workflow {} run {}", workflowId, execution.getRunId());
            return ResponseEntity.accepted()
                    .body(new IndexingResponse(workflowId, execution.getRunId(), "STARTED"));
        } catch (WorkflowExecutionAlreadyStarted e) {
            log.info("Workflow {} already running (run {})", workflowId, e.getExecution().getRunId());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new IndexingResponse(workflowId, e.getExecution().getRunId(), "ALREADY_RUNNING"));
        }
    }
}
