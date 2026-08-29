package org.kbase.ragindexer.configurations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("http-service-configurations")
public class HttpServiceConfiguration {
    private Map<String, HttpConfiguration> services;
    private ConnectionConfiguration connection;

    @Getter
    @Setter
    public static class HttpConfiguration {
        private String baseUrl;
        private Integer readTimeoutInMillis;
        private Integer connectionRequestTimeoutInMillis;

        private Integer maxRetries;
        private Integer retryIntervalInMillis;
        private Integer keepAliveInSeconds;
        private Integer idleConnectionEvictionInSeconds;
    }

    @Getter
    @Setter
    public static class ConnectionConfiguration {
        private Integer maxTotalConnections;
        private Integer maxConnectionsPerRoute;
        private Integer connectionTimeoutInMillis;
        private Integer socketTimeoutInMillis;
        private Integer timeToLiveInMinutes;
        private Integer inactivityValidationIntervalInSeconds;
    }

    @Bean
    public PoolingHttpClientConnectionManager connectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(this.connection.getMaxTotalConnections());
        connectionManager.setDefaultMaxPerRoute(this.connection.getMaxConnectionsPerRoute());
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(this.connection.getConnectionTimeoutInMillis()))
                .setSocketTimeout(Timeout.ofMilliseconds(this.connection.getSocketTimeoutInMillis()))
                .setTimeToLive(TimeValue.ofMinutes(this.connection.getTimeToLiveInMinutes()))
                .setValidateAfterInactivity(TimeValue.ofSeconds(this.connection.getInactivityValidationIntervalInSeconds()))
                .build());
        return connectionManager;
    }

    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        final String defaultServiceName = "default";
        HttpConfiguration defaultConfig = this.services.get(defaultServiceName);
        if (defaultConfig == null) {
            throw new IllegalStateException("Default service configuration is missing");
        }
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectionRequestTimeout(Timeout.ofMilliseconds(defaultConfig.getConnectionRequestTimeoutInMillis()))
                                .setResponseTimeout(Timeout.ofMilliseconds(defaultConfig.getReadTimeoutInMillis()))
                                .build()
                )
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(defaultConfig.getMaxRetries(), TimeValue.ofMilliseconds(defaultConfig.getRetryIntervalInMillis())))
                .setKeepAliveStrategy((resp, ctx) -> TimeValue.ofSeconds(defaultConfig.getKeepAliveInSeconds()))
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(defaultConfig.getIdleConnectionEvictionInSeconds()))
                .addRequestInterceptorFirst((req, entity, ctx) ->
                        req.setHeader("X-Request-Id", MDC.get("traceId")))
                .build();
    }
}
