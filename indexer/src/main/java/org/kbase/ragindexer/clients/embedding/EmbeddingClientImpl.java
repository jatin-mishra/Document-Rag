package org.kbase.ragindexer.clients.embedding;

import org.kbase.ragindexer.constants.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingClientImpl implements IEmbeddingClient {
    @Override
    public byte[] embed(EmbeddingModel model, List<String> contentBatch) {
        return new byte[0];
    }
}
