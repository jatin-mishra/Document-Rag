package org.kbase.ragindexer.configurations;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("chunkingConfiguration")
public class ChunkingConfiguration {
    String bucket;
    String path;
}
