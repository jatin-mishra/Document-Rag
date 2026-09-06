package org.kbase.ragindexer.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexingRequest {
    @NotBlank
    String documentId;
    @Min(0)
    Long fileSizeInBytes;

    @Override
    public String toString() {
        return "IndexingRequest{" +
                "documentId='" + documentId + '\'' +
                ", fileSizeInBytes=" + fileSizeInBytes +
                '}';
    }
}
