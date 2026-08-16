package org.kbase.ragindexer.api;

public record StartIndexingResponse(String workflowId, String runId, String status) {
}
