package org.kbase.ragindexer.clients.embedding;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.kbase.ragindexer.configurations.HttpServiceConfiguration;
import org.kbase.ragindexer.constants.EmbeddingModel;
import org.kbase.ragindexer.error.Error;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class EmbeddingClientImpl implements IEmbeddingClient {

    private static final String EMBED_PATH = "/api/embed";

    private final RestClient restClient;

    private final HttpServiceConfiguration.HttpConfiguration configuration;

    private final String serviceName = "embedding-service";

    public EmbeddingClientImpl(
            HttpServiceConfiguration configuration,
            CloseableHttpClient httpClient) {
        this.configuration = configuration.getServices().get(this.serviceName);

        // The shared CloseableHttpClient carries the `default` RequestConfig (5s response
        // timeout). Embedding is far slower than that -- a cold model load alone can take
        // tens of seconds -- so override the per-request timeouts on the factory rather
        // than widening the timeout for every other service sharing the pool.
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(this.configuration.getReadTimeoutInMillis()));
        requestFactory.setConnectionRequestTimeout(
                Duration.ofMillis(this.configuration.getConnectionRequestTimeoutInMillis()));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<float[]> embed(EmbeddingModel model, List<String> contentBatch) {

        if (contentBatch == null || contentBatch.isEmpty()) {
            return List.of();
        }

        EmbedResponse response = restClient.post()
                .uri(configuration.getBaseUrl() + EMBED_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new EmbedRequest(model.getModelName(), contentBatch))
                .retrieve()
                .body(EmbedResponse.class);

        if (response == null) {
            throw Error.internal_server_error.builder()
                    .message(String.format("empty response from %s for model %s (%d inputs)",
                            serviceName, model.getModelName(), contentBatch.size()))
                    .build();
        }

        response.validate(model, contentBatch.size());

        log.debug("embedded {} inputs with model {}", contentBatch.size(), model.getModelName());

        return response.embeddings();
    }
}
