package com.finance_control.shared.service;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Service for compressing and decompressing file data using Java's Deflater/Inflater APIs.
 * Provides automatic compression decision logic based on file type and size.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileCompressionService {

    private final AppProperties appProperties;

    private static final String COMPRESSED_EXTENSION = ".compressed";
    private static final Set<String> DEFAULT_COMPRESSED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf", "application/zip", "application/gzip",
        "application/x-gzip", "application/x-compress", "video/mp4",
        "video/mpeg", "audio/mpeg", "audio/mp3"
    );

    /**
     * Compresses byte array data using Deflater with the configured compression level.
     *
     * @param data the data to compress
     * @param level compression level (0-9, where 0 is no compression and 9 is maximum)
     * @return compressed byte array
     * @throws IOException if compression fails
     */
    public byte[] compress(byte[] data, int level) throws IOException {
        if (data == null || data.length == 0) {
            return data;
        }

        Deflater deflater = new Deflater(level);
        deflater.setInput(data);
        deflater.finish();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } finally {
            deflater.end();
        }
    }

    /**
     * Compresses byte array data using the default compression level from configuration.
     *
     * @param data the data to compress
     * @return compressed byte array
     * @throws IOException if compression fails
     */
    public byte[] compress(byte[] data) throws IOException {
        int level = appProperties.supabase().storage().compression().level();
        return compress(data, level);
    }

    /**
     * Decompresses byte array data using Inflater.
     *
     * @param compressedData the compressed data to decompress
     * @return decompressed byte array
     * @throws IOException if decompression fails
     */
    public byte[] decompress(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return compressedData;
        }

        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length * 2)) {
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (java.util.zip.DataFormatException e) {
            throw new IOException("Failed to decompress data", e);
        } finally {
            inflater.end();
        }
    }

    /**
     * Determines if a file should be compressed based on content type, size, and configuration.
     *
     * @param contentType the MIME type of the file
     * @param fileSize the size of the file in bytes
     * @return true if the file should be compressed
     */
    public boolean shouldCompress(String contentType, long fileSize) {
        com.finance_control.shared.config.properties.SupabaseProperties.CompressionProperties compression = appProperties.supabase().storage().compression();

        if (!compression.enabled()) {
            return false;
        }

        if (fileSize < compression.minFileSizeBytes()) {
            log.debug("File size {} is below minimum threshold {}, skipping compression", fileSize, compression.minFileSizeBytes());
            return false;
        }

        if (isAlreadyCompressed(contentType)) {
            log.debug("Content type {} is already compressed, skipping compression", contentType);
            return false;
        }

        return true;
    }

    /**
     * Checks if a content type represents an already-compressed format.
     *
     * @param contentType the MIME type to check
     * @return true if the content type is already compressed
     */
    public boolean isAlreadyCompressed(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        List<String> skipContentTypes = appProperties.supabase().storage().compression().skipContentTypes();
        if (skipContentTypes != null && !skipContentTypes.isEmpty()) {
            return skipContentTypes.contains(contentType.toLowerCase());
        }

        return DEFAULT_COMPRESSED_TYPES.contains(contentType.toLowerCase());
    }

    /**
     * Calculates the compression ratio as a percentage.
     *
     * @param originalSize the original file size in bytes
     * @param compressedSize the compressed file size in bytes
     * @return compression ratio (0.0 to 1.0, where 0.1 means 10% reduction)
     */
    public double calculateCompressionRatio(int originalSize, int compressedSize) {
        if (originalSize == 0) {
            return 0.0;
        }
        return 1.0 - ((double) compressedSize / originalSize);
    }

    /**
     * Checks if compression should be applied based on the reduction ratio threshold.
     *
     * @param originalSize the original file size in bytes
     * @param compressedSize the compressed file size in bytes
     * @return true if compression ratio meets the minimum threshold
     */
    public boolean meetsCompressionThreshold(int originalSize, int compressedSize) {
        double ratio = calculateCompressionRatio(originalSize, compressedSize);
        double minRatio = appProperties.supabase().storage().compression().minReductionRatio();
        return ratio >= minRatio;
    }

    /**
     * Checks if a filename indicates a compressed file.
     *
     * @param fileName the filename to check
     * @return true if the filename ends with .compressed extension
     */
    public boolean isCompressedFile(String fileName) {
        return StringUtils.hasText(fileName) && fileName.endsWith(COMPRESSED_EXTENSION);
    }

    /**
     * Adds the .compressed extension to a filename.
     *
     * @param fileName the original filename
     * @return filename with .compressed extension
     */
    public String addCompressedExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return COMPRESSED_EXTENSION;
        }
        if (fileName.endsWith(COMPRESSED_EXTENSION)) {
            return fileName;
        }
        return fileName + COMPRESSED_EXTENSION;
    }

    /**
     * Removes the .compressed extension from a filename.
     *
     * @param fileName the filename with .compressed extension
     * @return filename without .compressed extension
     */
    public String removeCompressedExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return fileName;
        }
        if (fileName.endsWith(COMPRESSED_EXTENSION)) {
            return fileName.substring(0, fileName.length() - COMPRESSED_EXTENSION.length());
        }
        return fileName;
    }
}
