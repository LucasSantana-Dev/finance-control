package com.finance_control.unit.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.service.FileCompressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileCompressionServiceTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Supabase supabase;

    @Mock
    private AppProperties.Storage storage;

    @Mock
    private AppProperties.Compression compression;

    private FileCompressionService compressionService;

    @BeforeEach
    void setUp() {
        when(appProperties.supabase()).thenReturn(supabase);
        when(supabase.storage()).thenReturn(storage);
        when(storage.compression()).thenReturn(compression);

        when(compression.enabled()).thenReturn(true);
        when(compression.level()).thenReturn(6);
        when(compression.minReductionRatio()).thenReturn(0.1);
        when(compression.minFileSizeBytes()).thenReturn(1024L);
        when(compression.skipContentTypes()).thenReturn(List.of(
            "image/jpeg", "image/png", "application/pdf"
        ));

        compressionService = new FileCompressionService(appProperties);
    }

    @Test
    void compress_WithValidData_ShouldCompress() throws IOException {
        String testData = "This is a test string that should compress well. ".repeat(100);
        byte[] originalData = testData.getBytes();

        byte[] compressed = compressionService.compress(originalData);

        assertThat(compressed).isNotNull();
        assertThat(compressed.length).isLessThan(originalData.length);
    }

    @Test
    void compress_WithEmptyData_ShouldReturnEmpty() throws IOException {
        byte[] emptyData = new byte[0];

        byte[] compressed = compressionService.compress(emptyData);

        assertThat(compressed).isEmpty();
    }

    @Test
    void compress_WithNullData_ShouldReturnNull() throws IOException {
        byte[] compressed = compressionService.compress(null);

        assertThat(compressed).isNull();
    }

    @Test
    void compress_WithDifferentLevels_ShouldProduceDifferentSizes() throws IOException {
        String testData = "This is a test string that should compress well. ".repeat(100);
        byte[] originalData = testData.getBytes();

        byte[] lowCompression = compressionService.compress(originalData, 1);
        byte[] highCompression = compressionService.compress(originalData, 9);

        assertThat(lowCompression.length).isGreaterThanOrEqualTo(highCompression.length);
    }

    @Test
    void decompress_WithCompressedData_ShouldDecompress() throws IOException {
        String testData = "This is a test string that should compress well. ".repeat(100);
        byte[] originalData = testData.getBytes();
        byte[] compressed = compressionService.compress(originalData);

        byte[] decompressed = compressionService.decompress(compressed);

        assertThat(decompressed).isEqualTo(originalData);
    }

    @Test
    void decompress_WithEmptyData_ShouldReturnEmpty() throws IOException {
        byte[] emptyData = new byte[0];

        byte[] decompressed = compressionService.decompress(emptyData);

        assertThat(decompressed).isEmpty();
    }

    @Test
    void decompress_WithNullData_ShouldReturnNull() throws IOException {
        byte[] decompressed = compressionService.decompress(null);

        assertThat(decompressed).isNull();
    }

    @Test
    void decompress_WithInvalidData_ShouldThrowException() {
        byte[] invalidData = "not compressed data".getBytes();

        assertThatThrownBy(() -> compressionService.decompress(invalidData))
            .isInstanceOf(IOException.class);
    }

    @Test
    void shouldCompress_WithEnabledAndLargeFile_ShouldReturnTrue() {
        when(compression.enabled()).thenReturn(true);
        when(compression.minFileSizeBytes()).thenReturn(1024L);

        boolean result = compressionService.shouldCompress("text/plain", 2048L);

        assertThat(result).isTrue();
    }

    @Test
    void shouldCompress_WithDisabled_ShouldReturnFalse() {
        when(compression.enabled()).thenReturn(false);

        boolean result = compressionService.shouldCompress("text/plain", 2048L);

        assertThat(result).isFalse();
    }

    @Test
    void shouldCompress_WithSmallFile_ShouldReturnFalse() {
        when(compression.enabled()).thenReturn(true);
        when(compression.minFileSizeBytes()).thenReturn(1024L);

        boolean result = compressionService.shouldCompress("text/plain", 512L);

        assertThat(result).isFalse();
    }

    @Test
    void shouldCompress_WithAlreadyCompressedType_ShouldReturnFalse() {
        when(compression.enabled()).thenReturn(true);
        when(compression.minFileSizeBytes()).thenReturn(1024L);

        boolean result = compressionService.shouldCompress("image/jpeg", 2048L);

        assertThat(result).isFalse();
    }

    @Test
    void isAlreadyCompressed_WithJpeg_ShouldReturnTrue() {
        boolean result = compressionService.isAlreadyCompressed("image/jpeg");

        assertThat(result).isTrue();
    }

    @Test
    void isAlreadyCompressed_WithPng_ShouldReturnTrue() {
        boolean result = compressionService.isAlreadyCompressed("image/png");

        assertThat(result).isTrue();
    }

    @Test
    void isAlreadyCompressed_WithPdf_ShouldReturnTrue() {
        boolean result = compressionService.isAlreadyCompressed("application/pdf");

        assertThat(result).isTrue();
    }

    @Test
    void isAlreadyCompressed_WithTextPlain_ShouldReturnFalse() {
        boolean result = compressionService.isAlreadyCompressed("text/plain");

        assertThat(result).isFalse();
    }

    @Test
    void isAlreadyCompressed_WithNull_ShouldReturnFalse() {
        boolean result = compressionService.isAlreadyCompressed(null);

        assertThat(result).isFalse();
    }

    @Test
    void isAlreadyCompressed_WithEmpty_ShouldReturnFalse() {
        boolean result = compressionService.isAlreadyCompressed("");

        assertThat(result).isFalse();
    }

    @Test
    void calculateCompressionRatio_WithCompression_ShouldReturnPositiveRatio() {
        int originalSize = 1000;
        int compressedSize = 500;

        double ratio = compressionService.calculateCompressionRatio(originalSize, compressedSize);

        assertThat(ratio).isEqualTo(0.5);
    }

    @Test
    void calculateCompressionRatio_WithNoCompression_ShouldReturnZero() {
        int originalSize = 1000;
        int compressedSize = 1000;

        double ratio = compressionService.calculateCompressionRatio(originalSize, compressedSize);

        assertThat(ratio).isEqualTo(0.0);
    }

    @Test
    void calculateCompressionRatio_WithZeroOriginal_ShouldReturnZero() {
        int originalSize = 0;
        int compressedSize = 100;

        double ratio = compressionService.calculateCompressionRatio(originalSize, compressedSize);

        assertThat(ratio).isEqualTo(0.0);
    }

    @Test
    void meetsCompressionThreshold_WithSufficientReduction_ShouldReturnTrue() {
        when(compression.minReductionRatio()).thenReturn(0.1);

        boolean result = compressionService.meetsCompressionThreshold(1000, 800);

        assertThat(result).isTrue();
    }

    @Test
    void meetsCompressionThreshold_WithInsufficientReduction_ShouldReturnFalse() {
        when(compression.minReductionRatio()).thenReturn(0.1);

        boolean result = compressionService.meetsCompressionThreshold(1000, 950);

        assertThat(result).isFalse();
    }

    @Test
    void isCompressedFile_WithCompressedExtension_ShouldReturnTrue() {
        boolean result = compressionService.isCompressedFile("document.pdf.compressed");

        assertThat(result).isTrue();
    }

    @Test
    void isCompressedFile_WithoutCompressedExtension_ShouldReturnFalse() {
        boolean result = compressionService.isCompressedFile("document.pdf");

        assertThat(result).isFalse();
    }

    @Test
    void isCompressedFile_WithNull_ShouldReturnFalse() {
        boolean result = compressionService.isCompressedFile(null);

        assertThat(result).isFalse();
    }

    @Test
    void addCompressedExtension_WithFileName_ShouldAddExtension() {
        String result = compressionService.addCompressedExtension("document.pdf");

        assertThat(result).isEqualTo("document.pdf.compressed");
    }

    @Test
    void addCompressedExtension_WithAlreadyCompressed_ShouldNotDuplicate() {
        String result = compressionService.addCompressedExtension("document.pdf.compressed");

        assertThat(result).isEqualTo("document.pdf.compressed");
    }

    @Test
    void addCompressedExtension_WithNull_ShouldReturnExtensionOnly() {
        String result = compressionService.addCompressedExtension(null);

        assertThat(result).isEqualTo(".compressed");
    }

    @Test
    void removeCompressedExtension_WithCompressedFile_ShouldRemoveExtension() {
        String result = compressionService.removeCompressedExtension("document.pdf.compressed");

        assertThat(result).isEqualTo("document.pdf");
    }

    @Test
    void removeCompressedExtension_WithoutCompressedExtension_ShouldReturnOriginal() {
        String result = compressionService.removeCompressedExtension("document.pdf");

        assertThat(result).isEqualTo("document.pdf");
    }

    @Test
    void removeCompressedExtension_WithNull_ShouldReturnNull() {
        String result = compressionService.removeCompressedExtension(null);

        assertThat(result).isNull();
    }
}
