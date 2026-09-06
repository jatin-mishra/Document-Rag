package org.kbase.ragindexer.dto;

public record IndexingResponse(
        String workflowId,
        String runId,
        String status) {
}
