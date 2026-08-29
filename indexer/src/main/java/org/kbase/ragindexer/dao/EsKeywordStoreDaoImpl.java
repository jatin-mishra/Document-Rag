package org.kbase.ragindexer.dao;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.configurations.ElasticSearchConfiguration;
import org.kbase.ragindexer.constants.EsQueryType;
import org.kbase.ragindexer.context.model.ChunkKeywordStoreModel;
import org.kbase.ragindexer.error.Error;
import org.kbase.ragindexer.handlers.query.EsQueryTemplateLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class EsKeywordStoreDaoImpl implements IKeyWordDataStoreDao {
    private final EsQueryTemplateLoader queryLoader;
    private final ElasticsearchClient esClient;
    private final ElasticSearchConfiguration esConfig;
    private static final List<EsQueryType> mustToHave = List.of(
    );

    @PostConstruct
    public void validateStartUp(){
        if(!mustToHave.isEmpty() && !mustToHave.stream().allMatch(queryLoader::contains)){
            log.error("Failed to start application: because few ES query types not registered");
            throw Error.internal_server_error
                    .builder()
                    .message("Few ES queries are not registered!")
                    .build();
        }
    }

    @Override
    public String upsert(ChunkKeywordStoreModel document) {
        if (document == null || !StringUtils.hasText(document.getId())) {
            throw Error.bad_request.builder()
                    .message("A non-blank custom id is required to upsert a keyword document")
                    .build();
        }
        String index = esConfig.keywordIndex();
        try {
            IndexResponse response = esClient.index(request -> request
                    .index(index)
                    .id(document.getId())
                    .document(document));

            log.info("Upserted keyword document id: {}, index: {}, result: {}",
                    response.id(), index, response.result());
            return response.id();
        } catch (Exception exception) {
            log.error("Failed to upsert keyword document id: {}, index: {}",
                    document.getId(), index, exception);
            throw Error.internal_server_error.builder()
                    .details(Map.of("id", document.getId(), "index", index))
                    .build();
        }
    }
}
