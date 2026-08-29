package org.kbase.ragindexer.constants;

import lombok.Getter;

@Getter
public enum ChunkingStrategy {
    Naive("naive");

    private final String value;

    ChunkingStrategy(String value){
        this.value = value;
    }
}
