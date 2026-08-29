package org.kbase.ragindexer.clients.s3;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.kbase.ragindexer.dto.UploadToS3Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Component
public class S3Client implements IS3Client {

    private final RestClient restClient;
    private final long inMemoryThresholdBytes;
    private final software.amazon.awssdk.services.s3.S3Client s3;

    public S3Client(CloseableHttpClient httpClient,
                    software.amazon.awssdk.services.s3.S3Client s3,
                    @Value("${s3.download.in-memory-threshold-bytes:10485760}") long threshold) {
        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // No baseUrl: a presigned URL is absolute and passed whole to .uri(...)
        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.inMemoryThresholdBytes = threshold; // default 10 MB
        this.s3 = s3;
    }

    @Override
    public DownloadedDocument download(String presignedUrl, long sizeBytes) {
        validate(presignedUrl);
        return sizeBytes <= inMemoryThresholdBytes
                ? DownloadedDocument.inMemory(downloadToMemory(presignedUrl))
                : DownloadedDocument.onDisk(downloadToFile(presignedUrl), sizeBytes);
    }

    @Override
    public void uploadChunks(String bucket, String path, String content) {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);

        PutObjectResponse response = this.s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(path)
                        .contentType("text/plain; charset=utf-8")
                        .build(),
                RequestBody.fromBytes(body));

        UploadToS3Response.builder().bucketId(bucket).fileSize(response.size()).path(path).build();
    }

    private byte[] downloadToMemory(String url) {
        byte[] body = restClient.get().uri(url).retrieve().body(byte[].class);
        if (body == null) throw new S3DownloadException("Download failed, empty response body");
        log.info("Downloaded {} bytes in-memory", body.length);
        return body;
    }

    private Path downloadToFile(String url) {
        Path target = createTempFile(url);
        try {
            return restClient.get().uri(url).exchange((request, response) -> {
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new S3DownloadException("Download failed, status=" + response.getStatusCode());
                }
                long bytes = Files.copy(response.getBody(), target, StandardCopyOption.REPLACE_EXISTING);
                log.info("Downloaded {} bytes to {}", bytes, target);
                return target;
            });
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw (e instanceof S3DownloadException) ? e
                    : new S3DownloadException("Download failed for presigned URL", e);
        }
    }

    private URI validate(String presignedUrl) {
        if (presignedUrl == null || presignedUrl.isBlank()) {
            throw new S3DownloadException("Presigned URL must not be blank");
        }
        try {
            URI uri = new URI(presignedUrl);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
                throw new S3DownloadException("Unsupported URL scheme: " + scheme);
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new S3DownloadException("Malformed presigned URL", e);
        }
    }

    private Path createTempFile(String url) {
        try {
            return Files.createTempFile("s3-download-", suffixOf(url));
        } catch (IOException e) {
            throw new S3DownloadException("Could not create temp file", e);
        }
    }

    private String suffixOf(String url) {
        try {
            String path = new URI(url).getPath();
            int slash = path == null ? -1 : path.lastIndexOf('/');
            String name = slash >= 0 ? path.substring(slash + 1) : "";
            int dot = name.lastIndexOf('.');
            return dot > 0 ? name.substring(dot) : ".tmp";
        } catch (URISyntaxException e) {
            return ".tmp";
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete temp file {}", path, e);
        }
    }
}
