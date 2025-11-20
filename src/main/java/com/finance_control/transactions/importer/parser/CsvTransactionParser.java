package com.finance_control.transactions.importer.parser;

import com.finance_control.transactions.dto.importer.TransactionImportIssueDTO;
import com.finance_control.transactions.dto.importer.TransactionImportRequest;
import com.finance_control.transactions.importer.TransactionImportIssueType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Parser for CSV transaction import files.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvTransactionParser {

    public ParseResult parseCsv(MultipartFile file, TransactionImportRequest request) {
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
                        .collect(Collectors.toMap(this::normalizeKey, Function.identity(), (a, b) -> a));

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

                        entries.add(new ImportedEntry(index,
                                extract(record, externalIdColumn),
                                date,
                                description,
                                amount,
                                extract(record, typeColumn),
                                extract(record, subtypeColumn),
                                extract(record, sourceColumn),
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

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String sanitize(String value) {
        return value != null ? value.trim() : null;
    }

    private String safeErrorMessage(String message) {
        return StringUtils.hasText(message) ? message : "Failed to process entry";
    }

    private String resolveColumn(String desired, Map<String, String> headerLookup, String description) {
        String normalized = normalizeKey(desired);
        String actual = headerLookup.get(normalized);
        if (!StringUtils.hasText(actual)) {
            throw new IllegalArgumentException("Required " + description + " \"" + desired + "\" not found in CSV header");
        }
        return actual;
    }

    private String resolveOptionalColumn(String desired, Map<String, String> headerLookup) {
        if (!StringUtils.hasText(desired)) {
            return null;
        }
        return headerLookup.get(normalizeKey(desired));
    }

    private String extract(CSVRecord record, String column) {
        if (!StringUtils.hasText(column)) {
            return null;
        }
        return sanitize(record.get(column));
    }

    private LocalDateTime parseCsvDate(String raw, TransactionImportRequest.CsvConfiguration csv, ZoneId zoneId) {
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

    private BigDecimal parseCsvAmount(String raw, TransactionImportRequest.CsvConfiguration csv) {
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

    public record ImportedEntry(int lineNumber,
                                String externalId,
                                LocalDateTime date,
                                String description,
                                BigDecimal amount,
                                String typeValue,
                                String subtypeValue,
                                String sourceValue,
                                String categoryValue,
                                String subcategoryValue,
                                String sourceEntityValue) {
    }

    public record ParseResult(List<ImportedEntry> entries, List<TransactionImportIssueDTO> issues) {
        public ParseResult {
            entries = entries != null ? entries : List.of();
            issues = issues != null ? issues : List.of();
        }
    }
}
