package org.kbase.ragindexer.workflow.activity.chunking;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.clients.document.GetPresignedDocumentResponse;
import org.kbase.ragindexer.clients.document.IDocumentServiceClient;
import org.kbase.ragindexer.clients.s3.DownloadedDocument;
import org.kbase.ragindexer.clients.s3.IS3Client;
import org.kbase.ragindexer.constants.ChunkingStrategy;
import org.kbase.ragindexer.context.ChunkBatch;
import org.kbase.ragindexer.context.ChunkData;
import org.kbase.ragindexer.context.ChunkingActivityOutput;
import org.kbase.ragindexer.context.model.ChunkKeywordStoreModel;
import org.kbase.ragindexer.dao.IKeyWordDataStoreDao;
import org.kbase.ragindexer.dto.IndexingRequest;
import org.kbase.ragindexer.error.AppException;
import org.kbase.ragindexer.error.Error;
import org.kbase.ragindexer.strategies.chunking.ChunkingFactory;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.kbase.ragindexer.workflow.activity.ChunkingActivities;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_CHUNKING)
public class InMemoryChunkingActivity implements ChunkingActivities {

    private final IS3Client s3Client;

    private final IDocumentServiceClient documentServiceClient;

    private final IKeyWordDataStoreDao keyWordDataStoreDao;

    @Override
    public ChunkingActivityOutput fetchChunkAndStore(IndexingRequest request) throws AppException {
        log.info("starting chunking!...");
        GetPresignedDocumentResponse response = documentServiceClient.getPresignedDocument(request.getDocumentId());
        response.validate();
        log.info("received presigned url: {}", response.getUrl());
        try(DownloadedDocument document = s3Client.download(response.getUrl(), request.getFileSizeInBytes())){

            // didn't consider overlap
            List<ChunkBatch> chunkBatches = ChunkingFactory
                    .getStrategy(ChunkingStrategy.Naive)
                    .chunk(document.getLines(), request.getDocumentId());

            chunkBatches.stream().flatMap(lines -> lines.allChunks().stream()).forEach(this::storeChunkData);

            return ChunkingActivityOutput.builder()
                    .chunkIdBatches(chunkBatches.stream().map(ChunkBatch::chunkIds).toList())
                    .documentId(request.getDocumentId())
                    .build();

        }catch (Exception exception) {
            log.error("failed in chunking with exception: {}, documentId: {}", exception.getMessage(), request.getDocumentId(), exception);
            throw Error.internal_server_error.builder().message(exception.getMessage()).build();
        }
    }

    private void storeChunkData(ChunkData chunkData){
        log.info("chunked info: {}", chunkData.join());
        keyWordDataStoreDao.upsert(ChunkKeywordStoreModel.builder()
                        .id(chunkData.docId() + "_" + chunkData.chunkIdx())
                        .createdAt(Timestamp.from(Instant.now()))
                        .updatedAt(Timestamp.from(Instant.now()))
                        .createdBy("rag-indexer")
                        .updatedBy("rag-indexer")
                        .chunkIndex(chunkData.chunkIdx())
                        .content(chunkData.join())
                        .documentId(chunkData.docId())
                .build());

    }
}
