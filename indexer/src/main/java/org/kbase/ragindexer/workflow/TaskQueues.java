package org.kbase.ragindexer.workflow;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TaskQueues {
    public static final String DOCUMENT_CHUNKING = "DOCUMENT_CHUNKING";
    public static final String DOCUMENT_EMBEDDING = "DOCUMENT_EMBEDDING";
    public static final String DOCUMENT_INDEXING = "DOCUMENT_INDEXING";
}
