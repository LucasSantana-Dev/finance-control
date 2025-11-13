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
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.repository.TransactionRepository;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponse;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.banking.BankingResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.common.StatementResponse;
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.common.TransactionList;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponse;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardResponseMessageSet;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.DefaultStringConversion;
import com.webcohesion.ofx4j.io.OFXParseException;
import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for orchestrating statement imports (CSV or OFX) and delegating transaction creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionImportService {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

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

        ParseResult parseResult = switch (resolvedFormat) {
            case CSV -> parseCsv(file, request);
            case OFX -> parseOfx(file, request);
            default -> throw new IllegalArgumentException("Unsupported import format: " + resolvedFormat);
        };
        List<ImportedEntry> entries = parseResult.entries();

        TransactionImportResponse.TransactionImportResponseBuilder responseBuilder = TransactionImportResponse.builder()
                .dryRun(request.isDryRun())
                .totalEntries(entries.size());

        parseResult.issues().forEach(responseBuilder::issue);

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

    private ParseResult parseCsv(MultipartFile file, TransactionImportRequest request) {
        TransactionImportRequest.CsvConfiguration csv = request.getCsv();
        if (csv == null) {
            throw new IllegalArgumentException("CSV configuration is required");
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), csv.resolveCharset()))) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(csv.resolveDelimiter())
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setIgnoreSurroundingSpaces(true)
                    .setSkipHeaderRecord(true)
                    .setHeader()
                    .build();

            try (CSVParser parser = format.parse(reader)) {
                Map<String, String> headerLookup = parser.getHeaderMap().keySet().stream()
                        .collect(Collectors.toMap(TransactionImportService::normalizeKey, Function.identity(), (a, b) -> a));

                String dateColumn = resolveColumn(csv.getDateColumn(), headerLookup, "date column");
                String descriptionColumn = resolveColumn(csv.getDescriptionColumn(), headerLookup, "description column");
                String amountColumn = resolveColumn(csv.getAmountColumn(), headerLookup, "amount column");
                String typeColumn = resolveOptionalColumn(csv.getTypeColumn(), headerLookup);
                String subtypeColumn = resolveOptionalColumn(csv.getSubtypeColumn(), headerLookup);
                String sourceColumn = resolveOptionalColumn(csv.getSourceColumn(), headerLookup);
                String categoryColumn = resolveOptionalColumn(csv.getCategoryColumn(), headerLookup);
                String subcategoryColumn = resolveOptionalColumn(csv.getSubcategoryColumn(), headerLookup);
                String sourceEntityColumn = resolveOptionalColumn(csv.getSourceEntityColumn(), headerLookup);
                String externalIdColumn = resolveOptionalColumn(csv.getExternalIdColumn(), headerLookup);

                List<ImportedEntry> entries = new ArrayList<>();
                List<TransactionImportIssueDTO> issues = new ArrayList<>();
                int index = 0;
                for (CSVRecord record : parser) {
                    index++;
                    try {
                        LocalDateTime date = parseCsvDate(record.get(dateColumn), csv, request.resolveZoneId());
                        BigDecimal amount = parseCsvAmount(record.get(amountColumn), csv);
                        String description = sanitize(record.get(descriptionColumn));
                        if (!StringUtils.hasText(description)) {
                            throw new IllegalArgumentException("Description cannot be blank");
                        }

                        TransactionType detectedType = resolveTypeFromColumn(record, typeColumn, TransactionType.class, TransactionImportService::mapTransactionTypeValue);
                        TransactionSubtype detectedSubtype = resolveTypeFromColumn(record, subtypeColumn, TransactionSubtype.class, TransactionImportService::mapTransactionSubtypeValue);
                        TransactionSource detectedSource = resolveTypeFromColumn(record, sourceColumn, TransactionSource.class, TransactionImportService::mapTransactionSourceValue);

                        entries.add(new ImportedEntry(index,
                                extract(record, externalIdColumn),
                                date,
                                description,
                                amount,
                                detectedType,
                                detectedSubtype,
                                detectedSource,
                                extract(record, categoryColumn),
                                extract(record, subcategoryColumn),
                                extract(record, sourceEntityColumn)));
                    } catch (Exception ex) {
                        issues.add(TransactionImportIssueDTO.builder()
                                .lineNumber(index)
                                .externalReference(extract(record, externalIdColumn))
                                .message(safeErrorMessage(ex.getMessage()))
                                .type(TransactionImportIssueType.PARSING_ERROR)
                                .build());
                    }
                }

                return new ParseResult(entries, issues);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read CSV file", ex);
        }
    }

    private ParseResult parseOfx(MultipartFile file, TransactionImportRequest request) {
        List<ImportedEntry> entries = new ArrayList<>();
        List<TransactionImportIssueDTO> issues = new ArrayList<>();
        try (var input = file.getInputStream();
             var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {

            var stringConversion = new DefaultStringConversion(request.resolveZoneId().getId());
            AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<>(ResponseEnvelope.class);
            unmarshaller.setConversion(stringConversion);

            ResponseEnvelope envelope = unmarshaller.unmarshal(reader);

            int index = 0;
            index = extractBankingTransactions(envelope, request, entries, index);
            extractCreditCardTransactions(envelope, request, entries, index);
            return new ParseResult(entries, issues);
        } catch (IOException | OFXParseException ex) {
            throw new IllegalArgumentException("Unable to parse OFX file", ex);
        }
    }

    private int extractBankingTransactions(ResponseEnvelope envelope, TransactionImportRequest request,
                                           List<ImportedEntry> entries, int startingIndex) {
        ResponseMessageSet messageSet = envelope.getMessageSet(MessageSetType.banking);
        if (!(messageSet instanceof BankingResponseMessageSet bankingResponse)) {
            return startingIndex;
        }

        int index = startingIndex;
        for (BankStatementResponseTransaction transaction : bankingResponse.getStatementResponses()) {
            BankStatementResponse message = transaction.getMessage();
            index = extractStatementTransactions(entries, request, index, message, TransactionSource.BANK_TRANSACTION);
        }
        return index;
    }

    private void extractCreditCardTransactions(ResponseEnvelope envelope, TransactionImportRequest request,
                                               List<ImportedEntry> entries, int startingIndex) {
        ResponseMessageSet messageSet = envelope.getMessageSet(MessageSetType.creditcard);
        if (!(messageSet instanceof CreditCardResponseMessageSet creditCardResponse)) {
            return;
        }

        int index = startingIndex;
        for (CreditCardStatementResponseTransaction transaction : creditCardResponse.getStatementResponses()) {
            CreditCardStatementResponse message = transaction.getMessage();
            index = extractStatementTransactions(entries, request, index, message, TransactionSource.CREDIT_CARD);
        }
    }

    private int extractStatementTransactions(List<ImportedEntry> entries, TransactionImportRequest request,
                                             int index, StatementResponse response, TransactionSource fallbackSource) {
        if (response == null) {
            return index;
        }
        TransactionList transactionList = response.getTransactionList();
        if (transactionList == null) {
            return index;
        }
        List<com.webcohesion.ofx4j.domain.data.common.Transaction> ofxTransactions = transactionList.getTransactions();
        if (ofxTransactions == null) {
            return index;
        }
        ZoneId zoneId = request.resolveZoneId();
        for (com.webcohesion.ofx4j.domain.data.common.Transaction ofxTxn : ofxTransactions.stream()
                .sorted(Comparator.comparing(com.webcohesion.ofx4j.domain.data.common.Transaction::getDatePosted))
                .toList()) {
            index++;
            LocalDateTime date = Optional.ofNullable(ofxTxn.getDatePosted())
                    .map(datePosted -> LocalDateTime.ofInstant(datePosted.toInstant(), zoneId))
                    .orElse(null);
            BigDecimal amount = Optional.ofNullable(ofxTxn.getAmount())
                    .map(value -> BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP))
                    .orElse(null);
            String description = Optional.ofNullable(ofxTxn.getMemo()).filter(StringUtils::hasText)
                    .orElseGet(() -> sanitize(ofxTxn.getName()));

            TransactionType detectedType = mapOfxTransactionType(ofxTxn.getTransactionType(), amount);

            entries.add(new ImportedEntry(index,
                    ofxTxn.getId(),
                    date,
                    description,
                    amount,
                    detectedType,
                    null,
                    fallbackSource,
                    null,
                    null,
                    null));
        }
        return index;
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
        dto.setSubcategoryId(resolveMapping(entry.subcategoryValue(), request.getDefaultSubcategoryId(), subcategoryMappings));
        dto.setSourceEntityId(resolveMapping(entry.sourceEntityValue(), request.getDefaultSourceEntityId(), sourceEntityMappings));

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

    private static String sanitize(String value) {
        return value != null ? value.trim() : null;
    }

    private static String safeErrorMessage(String message) {
        return StringUtils.hasText(message) ? message : "Failed to process entry";
    }

    private static String resolveColumn(String desired, Map<String, String> headerLookup, String description) {
        String normalized = normalizeKey(desired);
        String actual = headerLookup.get(normalized);
        if (!StringUtils.hasText(actual)) {
            throw new IllegalArgumentException("Required " + description + " \"" + desired + "\" not found in CSV header");
        }
        return actual;
    }

    private static String resolveOptionalColumn(String desired, Map<String, String> headerLookup) {
        if (!StringUtils.hasText(desired)) {
            return null;
        }
        return headerLookup.get(normalizeKey(desired));
    }

    private static String extract(CSVRecord record, String column) {
        if (!StringUtils.hasText(column)) {
            return null;
        }
        return sanitize(record.get(column));
    }

    private static LocalDateTime parseCsvDate(String raw, TransactionImportRequest.CsvConfiguration csv, ZoneId zoneId) {
        if (!StringUtils.hasText(raw)) {
            throw new IllegalArgumentException("Date value is missing");
        }
        final ZoneId effectiveZone = zoneId != null ? zoneId : ZoneId.systemDefault();
        for (String pattern : csv.safeDatePatterns()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, csv.resolveLocale());
                TemporalAccessor accessor = formatter.parse(raw);
                LocalDate date = accessor.query(TemporalQueries.localDate());
                if (date != null) {
                    return date.atStartOfDay(effectiveZone).toLocalDateTime();
                }
                LocalDateTime dateTime = accessor.query(TemporalQueries.localDate()) != null
                        && accessor.query(TemporalQueries.localTime()) != null
                        ? LocalDateTime.from(accessor)
                        : null;
                if (dateTime != null) {
                    return dateTime;
                }
            } catch (DateTimeParseException ignored) {
                // Try next pattern
            }
        }
        throw new IllegalArgumentException("Unable to parse date \"" + raw + "\" using configured patterns");
    }

    private static BigDecimal parseCsvAmount(String raw, TransactionImportRequest.CsvConfiguration csv) {
        if (!StringUtils.hasText(raw)) {
            throw new IllegalArgumentException("Amount value is missing");
        }
        String normalized = raw.trim();
        boolean isNegative = normalized.startsWith("(") && normalized.endsWith(")");
        if (isNegative) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        normalized = normalized.replace(" ", "");
        char decimalSeparator = csv.resolveDecimalSeparator();
        char groupingSeparator = csv.resolveGroupingSeparator();
        normalized = normalized.replace(String.valueOf(groupingSeparator), "");
        normalized = normalized.replace(String.valueOf(decimalSeparator), ".");
        normalized = normalized.replace(",", ".");
        normalized = normalized.replace("+", "");
        if (normalized.endsWith("-")) {
            normalized = "-" + normalized.substring(0, normalized.length() - 1);
        }
        try {
            BigDecimal amount = new BigDecimal(normalized);
            if (isNegative || amount.signum() < 0) {
                return amount.abs().negate();
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unable to parse amount \"" + raw + "\"");
        }
    }

    private static TransactionType mapOfxTransactionType(com.webcohesion.ofx4j.domain.data.common.TransactionType ofxType,
                                                         BigDecimal amount) {
        if (ofxType == null) {
            return amount != null && amount.signum() < 0 ? TransactionType.EXPENSE : TransactionType.INCOME;
        }
        return switch (ofxType) {
            case CREDIT, INT, DIV, REPEATPMT, IN, OTHER -> TransactionType.INCOME;
            case DEBIT, PAYMENT, ATM, POS, DIRECTDEBIT, DIRECTDEP, DEP, CHECK, FEE, SRVCHG, XFER, CASH, OUT ->
                    TransactionType.EXPENSE;
            default -> amount != null && amount.signum() < 0 ? TransactionType.EXPENSE : TransactionType.INCOME;
        };
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

    private static <T> T resolveTypeFromColumn(CSVRecord record, String column,
                                               Class<T> type,
                                               Function<String, T> fallbackParser) {
        if (!StringUtils.hasText(column)) {
            return null;
        }
        String rawValue = sanitize(record.get(column));
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }
        T parsed = fallbackParser.apply(rawValue);
        if (parsed != null) {
            return parsed;
        }
        throw new IllegalArgumentException("Unable to map value \"" + rawValue + "\" for column " + column + " to " + type.getSimpleName());
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
     * Internal representation of an imported entry before mapping to the domain DTO.
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

    private record ParseResult(List<ImportedEntry> entries, List<TransactionImportIssueDTO> issues) {
        private ParseResult {
            entries = entries != null ? entries : List.of();
            issues = issues != null ? issues : List.of();
        }
    }
}

