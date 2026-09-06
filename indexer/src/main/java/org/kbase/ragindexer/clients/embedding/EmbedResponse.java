package org.kbase.ragindexer.clients.embedding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.kbase.ragindexer.constants.EmbeddingModel;
import org.kbase.ragindexer.error.AppException;
import org.kbase.ragindexer.error.Error;

import java.util.List;

/**
 * Response of Ollama's {@code POST /api/embed}. The service also returns timing
 * fields (total_duration, load_duration, prompt_eval_count) that we do not model.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record EmbedResponse(String model, List<float[]> embeddings) {

    /**
     * Fails fast on a shape mismatch so the caller can index into {@link #embeddings()}
     * without further checks, and so a wrong/missing model surfaces here rather than as
     * a pgvector dimension error deep inside the DAO.
     */
    void validate(EmbeddingModel expectedModel, int expectedCount) throws AppException {
        if (embeddings == null || embeddings.size() != expectedCount) {
            throw Error.internal_server_error.builder()
                    .message(String.format("embedding-service returned %s vectors for %d inputs (model %s)",
                            embeddings == null ? "null" : embeddings.size(),
                            expectedCount, expectedModel.getModelName()))
                    .build();
        }
        for (int i = 0; i < embeddings.size(); i++) {
            float[] vector = embeddings.get(i);
            if (vector == null || vector.length != expectedModel.getDimension()) {
                throw Error.internal_server_error.builder()
                        .message(String.format("embedding %d has dimension %s, expected %d for model %s",
                                i, vector == null ? "null" : vector.length,
                                expectedModel.getDimension(), expectedModel.getModelName()))
                        .build();
            }
        }
    }
}
