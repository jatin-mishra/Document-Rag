package org.kbase.ragindexer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ragindexer.services")
public record ServiceProperties(
        String documentServiceUrl,
        String embeddingServiceUrl,
        String vectorStoreUrl) {
}
