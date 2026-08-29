package org.kbase.ragindexer.clients.s3;

import java.util.List;

public interface IS3Client {
    /**
     * Downloads the object at a presigned URL, choosing in-memory vs temp-file by size.
     *
     * @param presignedUrl self-authenticating absolute HTTP(S) URL
     * @param sizeBytes    known object size; &lt;= threshold streams to heap, else to disk
     * @return an AutoCloseable document; caller must close() to release the temp file
     * @throws S3DownloadException on invalid URL, non-2xx response, or IO failure
     */
    DownloadedDocument download(String presignedUrl, long sizeBytes);

    void uploadChunks(String bucketId, String path, String content);
}
