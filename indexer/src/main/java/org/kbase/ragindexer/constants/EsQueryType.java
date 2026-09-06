package org.kbase.ragindexer.constants;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum EsQueryType {
    AddDocument("add_document.json");

    private final String fileName;

    EsQueryType(String fileName){
        this.fileName = fileName;
    }

    public static boolean isWhitelisted(String name){
        return Arrays.stream(EsQueryType.values()).anyMatch(type -> type.getFileName().equals(name));
    }

    public static EsQueryType fromFileName(String name){
        return Arrays.stream(EsQueryType.values())
                .filter(type -> type.getFileName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
