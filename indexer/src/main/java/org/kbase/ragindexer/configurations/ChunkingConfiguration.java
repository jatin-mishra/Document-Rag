package org.kbase.ragindexer.configurations;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("chunking-configuration")
public class ChunkingConfiguration {
    String bucket;
    String path;
}