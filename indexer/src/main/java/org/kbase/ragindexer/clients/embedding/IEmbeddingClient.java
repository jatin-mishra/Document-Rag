package org.kbase.ragindexer.clients.embedding;

import org.kbase.ragindexer.constants.EmbeddingModel;

import java.util.List;

public interface IEmbeddingClient {
    byte[] embed(EmbeddingModel model, List<String> contentBatch);
}
