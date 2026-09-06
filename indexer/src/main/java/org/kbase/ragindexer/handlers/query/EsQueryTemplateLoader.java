package org.kbase.ragindexer.handlers.query;


import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.kbase.ragindexer.constants.EsQueryType;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@NoArgsConstructor
public class EsQueryTemplateLoader {

    private final Map<EsQueryType, String> queries = new HashMap<>();

    @PostConstruct
    public void loadQueries() throws IOException {
        ResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();

        Resource[] resources =
                resolver.getResources("classpath:/queries/*.json");

        for (Resource resource : resources) {
            String fileName = resource.getFilename();

            if (fileName != null && EsQueryType.isWhitelisted(fileName)) {
                String content = StreamUtils.copyToString(
                        resource.getInputStream(),
                        StandardCharsets.UTF_8
                );
                queries.put(EsQueryType.fromFileName(fileName), content);
            }
        }
    }

    public String getQuery(EsQueryType queryType) {
        return queries.get(queryType);
    }

    public boolean contains(EsQueryType queryType){
        return queries.containsKey(queryType);
    }
}
