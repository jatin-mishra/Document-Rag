package org.kbase.ragindexer.workflow.activity.chunking;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kbase.ragindexer.clients.document.GetPresignedDocumentResponse;
import org.kbase.ragindexer.clients.document.IDocumentServiceClient;
import org.kbase.ragindexer.clients.s3.DownloadedDocument;
import org.kbase.ragindexer.clients.s3.IS3Client;
import org.kbase.ragindexer.configurations.ChunkingConfiguration;
import org.kbase.ragindexer.context.ChunkBatch;
import org.kbase.ragindexer.context.ChunkData;
import org.kbase.ragindexer.context.ChunkingActivityOutput;
import org.kbase.ragindexer.context.model.ChunkKeywordStoreModel;
import org.kbase.ragindexer.dao.IKeyWordDataStoreDao;
import org.kbase.ragindexer.dto.IndexingRequest;
import org.kbase.ragindexer.error.AppException;
import org.kbase.ragindexer.strategies.chunking.ChunkingFactory;
import org.kbase.ragindexer.workflow.TaskQueues;
import org.kbase.ragindexer.workflow.activity.ChunkingActivities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActivityImpl(taskQueues = TaskQueues.DOCUMENT_CHUNKING)
public class InMemoryChunkingActivity implements ChunkingActivities {

    private final IS3Client s3Client;

    private final IDocumentServiceClient documentServiceClient;

    private final ChunkingConfiguration chunkingConfiguration;

    private final IKeyWordDataStoreDao keyWordDataStoreDao;

    @Override
    public ChunkingActivityOutput fetchChunkAndStore(IndexingRequest request) throws AppException {
        GetPresignedDocumentResponse response = documentServiceClient.getPresignedDocument(request.documentId());
        response.validate();
        try(DownloadedDocument document = s3Client.download(response.getDetails().getUrl(), request.fileSizeInBytes())){

            // didn't consider overlap
            List<ChunkBatch> chunkBatches = ChunkingFactory
                    .getStrategy(response.getChunkingStrategy())
                    .chunk(document.getLines(), chunkingConfiguration.getPath(), request.documentId());

            chunkBatches.stream().flatMap(lines -> lines.allChunks().stream()).forEach(this::storeChunkData);

            return ChunkingActivityOutput.builder()
                    .chunkBatchList(chunkBatches)
                    .documentId(request.documentId())
                    .bucket(chunkingConfiguration.getBucket())
                    .build();

        }catch (Exception exception) {
            log.error("failed in chunking with exception: {}, documentId: {}", exception.getMessage(), request.documentId(), exception);
            throw exception;
        }
    }

    private void storeChunkData(ChunkData chunkData){
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
