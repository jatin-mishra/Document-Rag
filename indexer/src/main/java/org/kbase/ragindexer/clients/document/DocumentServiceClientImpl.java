package org.kbase.ragindexer.clients.document;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.kbase.ragindexer.configurations.HttpServiceConfiguration;
import org.kbase.ragindexer.error.Error;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Primary
@Component
public class DocumentServiceClientImpl implements IDocumentServiceClient {

    private final RestClient restClient;

    private final HttpServiceConfiguration.HttpConfiguration configuration;

    private final String serviceName = "document-service";

    public DocumentServiceClientImpl(
            HttpServiceConfiguration configuration,
            CloseableHttpClient httpClient){
        this.configuration = configuration.getServices().get(this.serviceName);
        // override if needed
        this.restClient = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }



    @Override
    public GetPresignedDocumentResponse getPresignedDocument(String documentId) {

        GetPresignedDocumentResponse response = restClient.post()
                .uri(String.format(configuration.getBaseUrl() + "/document/%s/presigned", documentId))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(GetPresignedDocumentResponse.class);

        if (response == null) {
            throw Error.internal_server_error.builder()
                    .message(String.format("empty response from %s for documentId %s", serviceName, documentId))
                    .build();
        }

        response.validate();

        return response;
    }
}
