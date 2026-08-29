package org.kbase.ragindexer.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record IndexingRequest(@NotBlank String documentId, @Min(0) Long fileSizeInBytes) {
}
