package org.kbase.ragindexer.constants;

import lombok.Getter;

@Getter
public enum EmbeddingModel {
    // modelName must match the tag reported by `ollama list`; dimension must match
    // the vector(N) column in chunk_vector_store and vector-store.embedding-dimension.
    BGE_M3("bge-m3", 1024);

    private final String modelName;
    private final int dimension;

    EmbeddingModel(String modelName, int dimension) {
        this.modelName = modelName;
        this.dimension = dimension;
    }
}
