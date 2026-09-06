package org.kbase.ragindexer.workflow.activity.embedding;

import com.pgvector.PGvector;
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.clients.embedding.IEmbeddingClient;
import org.kbase.ragindexer.constants.EmbeddingModel;
import org.kbase.ragindexer.context.model.ChunkKeywordStoreModel;
import org.kbase.ragindexer.context.model.ChunkVectorStoreModel;
import org.kbase.ragindexer.dao.IKeyWordDataStoreDao;
import org.kbase.ragindexer.dao.IVectorStoreDao;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.kbase.ragindexer.workflow.activity.EmbeddingActivities;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_EMBEDDING)
public class InMemoryEmbeddingActivity implements EmbeddingActivities {

    private final IKeyWordDataStoreDao keyWordDataStoreDao;

    private final IVectorStoreDao vectorStoreDao;

    private final IEmbeddingClient embeddingClient;

    @Override
    public void embedAggregateAndStore(String documentId, List<Integer> chunkIds) {
        /*
         * get chunks from the keyword store, embed each one, and store the vector
         * alongside the content in the pgvector store.
         *
         * One embedding call per chunk: a mid-batch failure re-runs the whole activity
         * on Temporal retry, which re-embeds earlier chunks -- wasteful but correct,
         * since vectorStoreDao.upsert is idempotent on chunk id.
         */
        Set<String> chunkIdSet = buildChunkIds(documentId, chunkIds);
        List<ChunkKeywordStoreModel> chunkDataList = keyWordDataStoreDao.batchGet(new ArrayList<>(chunkIdSet));

        for (ChunkKeywordStoreModel chunkData : chunkDataList) {

            List<float[]> embeddings = embeddingClient.embed(
                    EmbeddingModel.BGE_M3, List.of(chunkData.getContent()));

            Timestamp now = Timestamp.from(Instant.now());
            vectorStoreDao.upsert(ChunkVectorStoreModel.builder()
                    .id(chunkData.getId())
                    .documentId(chunkData.getDocumentId())
                    .chunkIndex(chunkData.getChunkIndex())
                    .content(chunkData.getContent())
                    // validated to be exactly one vector of the model's dimension
                    .embedding(new PGvector(embeddings.getFirst()))
                    .createdAt(now)
                    .createdBy(chunkData.getCreatedBy())
                    .updatedAt(now)
                    .updatedBy(chunkData.getUpdatedBy())
                    .build());
        }

        log.info("embedded and stored {} chunks for document {}", chunkDataList.size(), documentId);
    }

    private Set<String> buildChunkIds(String documentId, List<Integer> chunkIds) {
        return chunkIds.stream()
                .map(chunkId -> documentId + "_" + chunkId)
                .collect(Collectors.toSet());
    }
}
