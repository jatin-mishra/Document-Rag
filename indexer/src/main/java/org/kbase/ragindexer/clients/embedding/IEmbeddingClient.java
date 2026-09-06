package org.kbase.ragindexer.clients.embedding;

import org.kbase.ragindexer.constants.EmbeddingModel;

import java.util.List;

public interface IEmbeddingClient {

    /**
     * Embeds a batch of texts in a single call.
     *
     * @return one vector per input, in input order; an empty list for an empty batch.
     */
    List<float[]> embed(EmbeddingModel model, List<String> contentBatch);
}
