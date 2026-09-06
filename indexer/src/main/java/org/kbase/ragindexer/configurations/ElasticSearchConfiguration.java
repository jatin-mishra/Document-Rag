package org.kbase.ragindexer.configurations;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "es")
public record ElasticSearchConfiguration (
            String scheme,
            String host,
            int port,
            String username,
            String password,
            String keywordIndex,
            int connectTimeoutMs,
            int socketTimeoutMs,
            int maxConnTotal,
            int maxConnPerRoute) {

    @Bean(destroyMethod = "close")
    RestClient restClient(ElasticSearchConfiguration props) {

        HttpHost host = new HttpHost(props.host(), props.port(), props.scheme());

        return RestClient.builder(host)
                .setRequestConfigCallback(rc -> rc
                        .setConnectTimeout(props.connectTimeoutMs())
                        .setSocketTimeout(props.socketTimeoutMs()))
                .setHttpClientConfigCallback(hc -> {
                    hc.setMaxConnTotal(props.maxConnTotal());
                    hc.setMaxConnPerRoute(props.maxConnPerRoute());
                    if (StringUtils.hasText(props.username())) {
                        var provider = new BasicCredentialsProvider();
                        provider.setCredentials(AuthScope.ANY,
                                new UsernamePasswordCredentials(props.username(), props.password()));
                        hc.setDefaultCredentialsProvider(provider);
                    }
                    return hc;
                })
                .build();
    }

    @Bean
    ElasticsearchClient esClient(RestClient restClient, ObjectMapper mapper) {
        return new ElasticsearchClient(
                new RestClientTransport(restClient, new JacksonJsonpMapper(mapper)));
    }
}
