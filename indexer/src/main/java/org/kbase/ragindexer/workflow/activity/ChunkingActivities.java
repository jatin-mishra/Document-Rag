package org.kbase.ragindexer.workflow.activity;

import io.temporal.activity.ActivityInterface;
import org.kbase.ragindexer.context.ChunkingActivityOutput;
import org.kbase.ragindexer.dto.IndexingRequest;
import org.kbase.ragindexer.error.AppException;


@ActivityInterface
public interface ChunkingActivities {
     ChunkingActivityOutput fetchChunkAndStore(IndexingRequest request) throws AppException;
}
