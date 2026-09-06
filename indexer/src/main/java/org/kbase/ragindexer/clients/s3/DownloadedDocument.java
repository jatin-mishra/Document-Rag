package org.kbase.ragindexer.clients.s3;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/** Abstracts an in-memory vs on-disk download so consumers read it identically. */
@Getter
@Slf4j
public final class DownloadedDocument implements AutoCloseable {

    private final Resource resource;
    private final Path tempFile;   // non-null only for the on-disk variant
    private final long size;

    private DownloadedDocument(Resource resource, Path tempFile, long size) {
        this.resource = resource;
        this.tempFile = tempFile;
        this.size = size;
    }

    static DownloadedDocument inMemory(byte[] bytes) {
        return new DownloadedDocument(new ByteArrayResource(bytes), null, bytes.length);
    }

    static DownloadedDocument onDisk(Path file, long size) {
        return new DownloadedDocument(new FileSystemResource(file), file, size);
    }

    public InputStream getInputStream() throws IOException { return resource.getInputStream(); }

    /**
     * Reads the document as UTF-8 text lines. Works identically for the in-memory and on-disk
     * variants because both read through the underlying {@link Resource}.
     * <p>
     * Loads all lines into memory; fine for docs below the on-disk threshold. For very large
     * on-disk files, prefer a streaming reader over {@link #getInputStream()}.
     */
    public List<String> getLines() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new S3DownloadException("Failed to read document lines", e);
        }
    }

    public long size() { return size; }

    public boolean isOnDisk() { return tempFile != null; }

    @Override
    public void close() {
        if (tempFile == null) return;
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.warn("Failed to delete temp file {}", tempFile, e);
        }
    }
}
