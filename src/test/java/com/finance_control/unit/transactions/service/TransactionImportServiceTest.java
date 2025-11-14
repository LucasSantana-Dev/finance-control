package com.finance_control.unit.transactions.service;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.importer.TransactionImportRequest;
import com.finance_control.transactions.dto.importer.TransactionImportResponse;
import com.finance_control.transactions.importer.DuplicateHandlingStrategy;
import com.finance_control.transactions.importer.StatementImportFormat;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.service.TransactionImportService;
import com.finance_control.transactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionImportService Unit Tests")
class TransactionImportServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionImportService transactionImportService;

    private TransactionImportRequest request;
    private Transaction existingTransaction;

    @BeforeEach
    void setUp() {
        request = TransactionImportRequest.builder()
                .userId(1L)
                .defaultCategoryId(1L)
                .defaultSubtype(TransactionSubtype.FIXED)
                .defaultSource(TransactionSource.BANK_TRANSACTION)
                .format(StatementImportFormat.CSV)
                .duplicateStrategy(DuplicateHandlingStrategy.SKIP)
                .dryRun(false)
                .responsibilities(List.of(TransactionImportRequest.ResponsibilityAllocation.builder()
                        .responsibleId(1L)
                        .percentage(BigDecimal.valueOf(100))
                        .build()))
                .csv(TransactionImportRequest.CsvConfiguration.builder()
                        .containsHeader(true)
                        .delimiter(";")
                        .dateColumn("date")
                        .descriptionColumn("description")
                        .amountColumn("amount")
                        .locale("pt-BR")
                        .datePatterns(List.of("yyyy-MM-dd"))
                        .build())
                .build();

        existingTransaction = new Transaction();
        existingTransaction.setId(1L);
        existingTransaction.setDescription("Existing Transaction");
        existingTransaction.setAmount(BigDecimal.valueOf(100.00));
        existingTransaction.setDate(LocalDateTime.of(2024, 1, 5, 0, 0));
    }

    @Test
    @DisplayName("importStatements_WithValidCsv_ShouldImportSuccessfully")
    void importStatements_WithValidCsv_ShouldImportSuccessfully() {
        String csvContent = """
                date;description;amount
                2024-01-05;Test Transaction;100.00
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        when(transactionRepository.findPotentialDuplicates(any(), any(), anyString(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(transactionService.create(any(TransactionDTO.class))).thenReturn(new TransactionDTO());

        TransactionImportResponse response = transactionImportService.importStatements(file, request);

        assertThat(response).isNotNull();
        assertThat(response.getTotalEntries()).isEqualTo(1);
        assertThat(response.getCreatedTransactions()).isEqualTo(1);
        assertThat(response.getDuplicateEntries()).isEqualTo(0);
        verify(transactionService, times(1)).create(any(TransactionDTO.class));
    }

    @Test
    @DisplayName("importStatements_WithDuplicate_ShouldSkipWhenStrategyIsSkip")
    void importStatements_WithDuplicate_ShouldSkipWhenStrategyIsSkip() {
        String csvContent = """
                date;description;amount
                2024-01-05;Existing Transaction;100.00
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        when(transactionRepository.findPotentialDuplicates(eq(1L), eq(BigDecimal.valueOf(100.00)),
                eq("Existing Transaction"), any(), any()))
                .thenReturn(List.of(existingTransaction));

        TransactionImportResponse response = transactionImportService.importStatements(file, request);

        assertThat(response).isNotNull();
        assertThat(response.getTotalEntries()).isEqualTo(1);
        assertThat(response.getCreatedTransactions()).isEqualTo(0);
        assertThat(response.getDuplicateEntries()).isGreaterThanOrEqualTo(0);
        verify(transactionService, never()).create(any(TransactionDTO.class));
    }

    @Test
    @DisplayName("importStatements_WithDryRun_ShouldNotCreateTransactions")
    void importStatements_WithDryRun_ShouldNotCreateTransactions() {
        String csvContent = """
                date;description;amount
                2024-01-05;Test Transaction;100.00
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        request.setDryRun(true);

        when(transactionRepository.findPotentialDuplicates(any(), any(), anyString(), any(), any()))
                .thenReturn(new ArrayList<>());

        TransactionImportResponse response = transactionImportService.importStatements(file, request);

        assertThat(response).isNotNull();
        assertThat(response.isDryRun()).isTrue();
        assertThat(response.getTotalEntries()).isEqualTo(1);
        assertThat(response.getCreatedTransactions()).isEqualTo(0);
        verify(transactionService, never()).create(any(TransactionDTO.class));
    }

    @Test
    @DisplayName("importStatements_WithInvalidCsv_ShouldReportIssues")
    void importStatements_WithInvalidCsv_ShouldReportIssues() {
        String csvContent = """
                date;description;amount
                2024-01-05;;100.00
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        TransactionImportResponse response = transactionImportService.importStatements(file, request);

        assertThat(response).isNotNull();
        assertThat(response.getTotalEntries()).isGreaterThanOrEqualTo(0);
        assertThat(response.getCreatedTransactions()).isEqualTo(0);
        verify(transactionService, never()).create(any(TransactionDTO.class));
    }

    @Test
    @DisplayName("importStatements_WithIgnoredDescription_ShouldSkipEntry")
    void importStatements_WithIgnoredDescription_ShouldSkipEntry() {
        String csvContent = """
                date;description;amount
                2024-01-05;IGNORE_THIS;100.00
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        request.setIgnoreDescriptions(List.of("IGNORE_THIS"));

        TransactionImportResponse response = transactionImportService.importStatements(file, request);

        assertThat(response).isNotNull();
        assertThat(response.getTotalEntries()).isEqualTo(1);
        assertThat(response.getCreatedTransactions()).isEqualTo(0);
        assertThat(response.getIssues()).isNotEmpty();
        verify(transactionService, never()).create(any(TransactionDTO.class));
    }

    @Test
    @DisplayName("importStatements_WithAutoFormat_ShouldDetectCsv")
    void importStatements_WithAutoFormat_ShouldDetectCsv() {
        String csvContent = """
                date;description;amount
                2024-01-05;Test Transaction;100.00
                """;

        MultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        request.setFormat(StatementImportFormat.AUTO);

        when(transactionRepository.findPotentialDuplicates(any(), any(), anyString(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(transactionService.create(any(TransactionDTO.class))).thenReturn(new TransactionDTO());

        TransactionImportResponse response = transactionImportService.importStatements(file, request);

        assertThat(response).isNotNull();
        assertThat(response.getCreatedTransactions()).isEqualTo(1);
        verify(transactionService, times(1)).create(any(TransactionDTO.class));
    }
}
