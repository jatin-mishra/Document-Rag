package org.kbase.ragindexer.workflow.activity.chunking;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.clients.document.GetPresignedDocumentResponse;
import org.kbase.ragindexer.clients.document.IDocumentServiceClient;
import org.kbase.ragindexer.clients.s3.DownloadedDocument;
import org.kbase.ragindexer.clients.s3.IS3Client;
import org.kbase.ragindexer.configurations.ChunkingConfiguration;
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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_CHUNKING)
public class InMemoryChunkingActivity implements ChunkingActivities {

    private final IS3Client s3Client;

    private final IDocumentServiceClient documentServiceClient;

    private final ChunkingConfiguration chunkingConfiguration;

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
                    .chunk(document.getLines(), chunkingConfiguration.getPath(), request.getDocumentId());

            chunkBatches.stream().flatMap(lines -> lines.allChunks().stream()).forEach(this::storeChunkData);

            return ChunkingActivityOutput.builder()
                    .chunkBatchList(chunkBatches)
                    .documentId(request.getDocumentId())
                    .bucket(chunkingConfiguration.getBucket())
                    .build();

        }catch (Exception exception) {
            log.error("failed in chunking with exception: {}, documentId: {}", exception.getMessage(), request.getDocumentId(), exception);
            throw Error.internal_server_error.builder().message(exception.getMessage()).build();
        }
    }

    private void storeChunkData(ChunkData chunkData){
        log.info("chunked info: {}", chunkData.join());
        s3Client.uploadChunks(chunkingConfiguration.getBucket(), chunkData.path(), chunkData.join());
        String[] brokenPath = chunkData.path().split("/");
        String[] fileNameAndExtension = brokenPath[brokenPath.length-1].split("\\.");
        keyWordDataStoreDao.upsert(ChunkKeywordStoreModel.builder()
                        .id(fileNameAndExtension[0])
                        .chunkIndex(chunkData.chunkIdx())
                        .content(chunkData.join())
                        .documentId(chunkData.docId())
                .build());
    }
}
