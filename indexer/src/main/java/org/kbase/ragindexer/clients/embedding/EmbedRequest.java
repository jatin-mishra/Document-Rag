package org.kbase.ragindexer.clients.embedding;

import java.util.List;

/**
 * Body of Ollama's {@code POST /api/embed}. Note this is the plural endpoint --
 * the legacy singular {@code /api/embeddings} takes {@code prompt} and returns a
 * single {@code embedding}, with no batching.
 */
record EmbedRequest(String model, List<String> input) {}
