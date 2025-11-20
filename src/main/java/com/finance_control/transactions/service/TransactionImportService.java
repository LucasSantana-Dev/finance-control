package com.finance_control.transactions.service;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.importer.TransactionImportIssueDTO;
import com.finance_control.transactions.dto.importer.TransactionImportRequest;
import com.finance_control.transactions.dto.importer.TransactionImportResponse;
import com.finance_control.transactions.importer.DuplicateHandlingStrategy;
import com.finance_control.transactions.importer.StatementImportFormat;
import com.finance_control.transactions.importer.TransactionImportIssueType;
import com.finance_control.transactions.importer.parser.CsvTransactionParser;
import com.finance_control.transactions.importer.parser.OfxTransactionParser;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.repository.TransactionRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for orchestrating statement imports (CSV or OFX) and
 * delegating transaction creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionImportService {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final CsvTransactionParser csvParser;
    private final OfxTransactionParser ofxParser;

    /**
     * Imports a bank statement creating transactions automatically.
     *
     * @param file    The uploaded statement file.
     * @param request Request metadata containing mapping and validation rules.
     * @return summary detailing the import outcome.
     */
    public TransactionImportResponse importStatements(MultipartFile file, @Valid TransactionImportRequest request) {
        StatementImportFormat resolvedFormat = resolveFormat(file, request);
        request.validateFor(resolvedFormat);

        var normalizedCategoryMappings = normalizeKeys(request.safeCategoryMappings());
        var normalizedSubcategoryMappings = normalizeKeys(request.safeSubcategoryMappings());
        var normalizedSourceEntityMappings = normalizeKeys(request.safeSourceEntityMappings());
        var normalizedTypeMappings = normalizeKeys(request.safeTypeMappings());
        var normalizedSubtypeMappings = normalizeKeys(request.safeSubtypeMappings());
        var normalizedSourceMappings = normalizeKeys(request.safeSourceMappings());
        var ignoreDescriptions = request.safeIgnoreDescriptions().stream()
                .map(TransactionImportService::normalizeKey)
                .collect(Collectors.toSet());

        List<ImportedEntry> entries;
        List<TransactionImportIssueDTO> issues = new ArrayList<>();
        switch (resolvedFormat) {
            case CSV -> {
                CsvTransactionParser.ParseResult csvResult = csvParser.parseCsv(file, request);
                entries = convertParseResult(csvResult);
                issues.addAll(csvResult.issues());
            }
            case OFX -> {
                OfxTransactionParser.ParseResult ofxResult = ofxParser.parseOfx(file, request);
                entries = convertOfxParseResult(ofxResult);
                issues.addAll(ofxResult.issues());
            }
            default -> throw new IllegalArgumentException("Unsupported import format: " + resolvedFormat);
        }

        TransactionImportResponse.TransactionImportResponseBuilder responseBuilder = TransactionImportResponse.builder()
                .dryRun(request.isDryRun())
                .totalEntries(entries.size());

        issues.forEach(responseBuilder::issue);

        int processed = 0;
        int created = 0;
        int duplicates = 0;

        for (ImportedEntry entry : entries) {
            if (shouldIgnore(entry, ignoreDescriptions)) {
                responseBuilder.issue(TransactionImportIssueDTO.builder()
                        .externalReference(entry.externalId())
                        .lineNumber(entry.lineNumber())
                        .message("Ignored due to configured description filter")
                        .type(TransactionImportIssueType.CONFIGURATION_REJECTED)
                        .build());
                continue;
            }

            try {
                TransactionDTO transactionDTO = buildTransactionDto(entry, request,
                        normalizedCategoryMappings,
                        normalizedSubcategoryMappings,
                        normalizedSourceEntityMappings,
                        normalizedTypeMappings,
                        normalizedSubtypeMappings,
                        normalizedSourceMappings);

                processed++;

                boolean isDuplicate = isDuplicate(transactionDTO, request.getUserId(), entry.date());
                if (isDuplicate && request.getDuplicateStrategy() == DuplicateHandlingStrategy.SKIP) {
                    duplicates++;
                    responseBuilder.issue(TransactionImportIssueDTO.builder()
                            .externalReference(entry.externalId())
                            .lineNumber(entry.lineNumber())
                            .message("Skipped duplicate entry")
                            .type(TransactionImportIssueType.DUPLICATE_SKIPPED)
                            .build());
                    continue;
                }

                if (request.isDryRun()) {
                    continue;
                }

                TransactionDTO persisted = transactionService.create(transactionDTO);
                created++;
                responseBuilder.createdTransaction(persisted);
            } catch (Exception ex) {
                log.debug("Failed to process import entry at line {}: {}", entry.lineNumber(), ex.getMessage());
                responseBuilder.issue(TransactionImportIssueDTO.builder()
                        .externalReference(entry.externalId())
                        .lineNumber(entry.lineNumber())
                        .message(safeErrorMessage(ex.getMessage()))
                        .type(TransactionImportIssueType.PARSING_ERROR)
                        .build());
            }
        }

        return responseBuilder
                .processedEntries(processed)
                .createdTransactions(created)
                .duplicateEntries(duplicates)
                .build();
    }

    private StatementImportFormat resolveFormat(MultipartFile file, TransactionImportRequest request) {
        if (request.getFormat() != null && request.getFormat() != StatementImportFormat.AUTO) {
            return request.getFormat();
        }
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (filename.endsWith(".csv")) {
            return StatementImportFormat.CSV;
        }
        if (filename.endsWith(".ofx")) {
            return StatementImportFormat.OFX;
        }
        String contentType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        if (contentType.contains("csv") || MediaType.TEXT_PLAIN_VALUE.equalsIgnoreCase(contentType)) {
            return StatementImportFormat.CSV;
        }
        if (contentType.contains("ofx") || contentType.contains("x-ofx")) {
            return StatementImportFormat.OFX;
        }
        throw new IllegalArgumentException("Unable to detect file format from filename or content type");
    }

    private List<ImportedEntry> convertParseResult(CsvTransactionParser.ParseResult parseResult) {
        return parseResult.entries().stream()
                .map(entry -> new ImportedEntry(
                        entry.lineNumber(),
                        entry.externalId(),
                        entry.date(),
                        entry.description(),
                        entry.amount(),
                        resolveTypeFromString(entry.typeValue(), TransactionType.class,
                                TransactionImportService::mapTransactionTypeValue),
                        resolveTypeFromString(entry.subtypeValue(), TransactionSubtype.class,
                                TransactionImportService::mapTransactionSubtypeValue),
                        resolveTypeFromString(entry.sourceValue(), TransactionSource.class,
                                TransactionImportService::mapTransactionSourceValue),
                        entry.categoryValue(),
                        entry.subcategoryValue(),
                        entry.sourceEntityValue()))
                .toList();
    }

    private <T> T resolveTypeFromString(String value, Class<T> type, Function<String, T> parser) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return parser.apply(value);
    }

    private List<ImportedEntry> convertOfxParseResult(OfxTransactionParser.ParseResult parseResult) {
        return parseResult.entries().stream()
                .map(entry -> new ImportedEntry(
                        entry.lineNumber(),
                        entry.externalId(),
                        entry.date(),
                        entry.description(),
                        entry.amount(),
                        entry.type(),
                        (TransactionSubtype) null,
                        entry.source(),
                        entry.categoryValue(),
                        entry.subcategoryValue(),
                        entry.sourceEntityValue()))
                .toList();
    }

    private TransactionDTO buildTransactionDto(ImportedEntry entry,
            TransactionImportRequest request,
            Map<String, Long> categoryMappings,
            Map<String, Long> subcategoryMappings,
            Map<String, Long> sourceEntityMappings,
            Map<String, TransactionType> typeMappings,
            Map<String, TransactionSubtype> subtypeMappings,
            Map<String, TransactionSource> sourceMappings) {
        if (entry.date() == null) {
            throw new IllegalArgumentException("Transaction date is required");
        }
        if (entry.amount() == null) {
            throw new IllegalArgumentException("Transaction amount is required");
        }

        TransactionDTO dto = new TransactionDTO();
        dto.setUserId(request.getUserId());
        dto.setDescription(entry.description());
        dto.setDate(entry.date());
        dto.setAmount(entry.amount().abs());
        dto.setCategoryId(resolveCategoryId(entry.categoryValue(), request.getDefaultCategoryId(), categoryMappings));
        dto.setSubcategoryId(
                resolveMapping(entry.subcategoryValue(), request.getDefaultSubcategoryId(), subcategoryMappings));
        dto.setSourceEntityId(
                resolveMapping(entry.sourceEntityValue(), request.getDefaultSourceEntityId(), sourceEntityMappings));

        TransactionType type = resolveTransactionType(entry, request, typeMappings);
        dto.setType(type);

        TransactionSubtype subtype = resolveTransactionSubtype(entry, request, subtypeMappings);
        dto.setSubtype(subtype);

        TransactionSource source = resolveTransactionSource(entry, request, sourceMappings);
        dto.setSource(source);

        dto.setResponsibilities(request.getResponsibilities().stream()
                .map(allocation -> {
                    var resp = new com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO();
                    resp.setResponsibleId(allocation.getResponsibleId());
                    resp.setPercentage(allocation.getPercentage());
                    resp.setNotes(allocation.getNotes());
                    return resp;
                })
                .toList());

        return dto;
    }

    private boolean isDuplicate(TransactionDTO dto, Long userId, LocalDateTime importDate) {
        LocalDateTime candidateDate = Optional.ofNullable(dto.getDate()).orElse(importDate);
        if (candidateDate == null) {
            return false;
        }
        LocalDate dateOnly = candidateDate.toLocalDate();
        LocalDateTime startOfDay = dateOnly.atStartOfDay();
        LocalDateTime endOfDay = dateOnly.plusDays(1).atStartOfDay().minusNanos(1);
        List<Transaction> potential = transactionRepository.findPotentialDuplicates(
                userId,
                dto.getAmount(),
                dto.getDescription(),
                startOfDay,
                endOfDay);
        return !potential.isEmpty();
    }

    private static boolean shouldIgnore(ImportedEntry entry, Set<String> ignoredDescriptions) {
        if (ignoredDescriptions.isEmpty()) {
            return false;
        }
        String description = normalizeKey(entry.description());
        return ignoredDescriptions.contains(description);
    }

    private static <T> Map<String, T> normalizeKeys(Map<String, T> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return source.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> normalizeKey(entry.getKey()),
                        Map.Entry::getValue,
                        (left, right) -> left,
                        HashMap::new));
    }

    private static String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safeErrorMessage(String message) {
        return StringUtils.hasText(message) ? message : "Failed to process entry";
    }

    private static TransactionType resolveTransactionType(ImportedEntry entry,
            TransactionImportRequest request,
            Map<String, TransactionType> typeMappings) {
        if (entry.detectedType() != null) {
            return entry.detectedType();
        }
        if (StringUtils.hasText(entry.categoryValue())) {
            TransactionType mapped = typeMappings.get(normalizeKey(entry.categoryValue()));
            if (mapped != null) {
                return mapped;
            }
        }
        if (entry.amount() != null && entry.amount().signum() < 0) {
            return TransactionType.EXPENSE;
        }
        if (entry.amount() != null && entry.amount().signum() > 0) {
            return TransactionType.INCOME;
        }
        if (request.getDefaultType() != null) {
            return request.getDefaultType();
        }
        throw new IllegalArgumentException("Unable to determine transaction type");
    }

    private static TransactionSubtype resolveTransactionSubtype(ImportedEntry entry,
            TransactionImportRequest request,
            Map<String, TransactionSubtype> subtypeMappings) {
        if (entry.detectedSubtype() != null) {
            return entry.detectedSubtype();
        }
        if (StringUtils.hasText(entry.categoryValue())) {
            TransactionSubtype mapped = subtypeMappings.get(normalizeKey(entry.categoryValue()));
            if (mapped != null) {
                return mapped;
            }
        }
        return request.getDefaultSubtype();
    }

    private static TransactionSource resolveTransactionSource(ImportedEntry entry,
            TransactionImportRequest request,
            Map<String, TransactionSource> sourceMappings) {
        if (entry.detectedSource() != null) {
            return entry.detectedSource();
        }
        if (StringUtils.hasText(entry.sourceEntityValue())) {
            TransactionSource mapped = sourceMappings.get(normalizeKey(entry.sourceEntityValue()));
            if (mapped != null) {
                return mapped;
            }
        }
        return request.getDefaultSource();
    }

    private static Long resolveCategoryId(String categoryValue, Long defaultId, Map<String, Long> mappings) {
        Long mapped = resolveMapping(categoryValue, null, mappings);
        if (mapped != null) {
            return mapped;
        }
        if (defaultId != null) {
            return defaultId;
        }
        throw new IllegalArgumentException("No category mapping or default category provided");
    }

    private static <T> T resolveMapping(String value, T defaultValue, Map<String, T> mappings) {
        if (StringUtils.hasText(value)) {
            T mapped = mappings.get(normalizeKey(value));
            if (mapped != null) {
                return mapped;
            }
        }
        return defaultValue;
    }

    private static TransactionType mapTransactionTypeValue(String raw) {
        try {
            return TransactionType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static TransactionSubtype mapTransactionSubtypeValue(String raw) {
        try {
            return TransactionSubtype.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static TransactionSource mapTransactionSourceValue(String raw) {
        try {
            return TransactionSource.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Internal representation of an imported entry before mapping to the domain
     * DTO.
     */
    private record ImportedEntry(int lineNumber,
            String externalId,
            LocalDateTime date,
            String description,
            BigDecimal amount,
            TransactionType detectedType,
            TransactionSubtype detectedSubtype,
            TransactionSource detectedSource,
            String categoryValue,
            String subcategoryValue,
            String sourceEntityValue) {
    }
}
