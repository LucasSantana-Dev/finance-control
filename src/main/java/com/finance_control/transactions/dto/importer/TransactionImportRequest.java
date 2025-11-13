package com.finance_control.transactions.dto.importer;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.importer.DuplicateHandlingStrategy;
import com.finance_control.transactions.importer.StatementImportFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Request payload describing metadata required to import transaction statements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionImportRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long defaultCategoryId;

    private Long defaultSubcategoryId;

    private Long defaultSourceEntityId;

    private TransactionType defaultType;

    @NotNull
    private TransactionSubtype defaultSubtype;

    @NotNull
    private TransactionSource defaultSource;

    @Builder.Default
    private StatementImportFormat format = StatementImportFormat.AUTO;

    @Builder.Default
    private DuplicateHandlingStrategy duplicateStrategy = DuplicateHandlingStrategy.SKIP;

    @Builder.Default
    private boolean dryRun = false;

    /**
     * Timezone expected for date-only statements (defaults to system default).
     */
    @Builder.Default
    private String timezone = ZoneId.systemDefault().getId();

    /**
     * Configures parsing behaviour for CSV statements.
     */
    @Valid
    private CsvConfiguration csv;

    @Singular("categoryMapping")
    private Map<String, Long> categoryMappings;

    @Singular("subcategoryMapping")
    private Map<String, Long> subcategoryMappings;

    @Singular("sourceEntityMapping")
    private Map<String, Long> sourceEntityMappings;

    @Singular("typeMapping")
    private Map<String, TransactionType> typeMappings;

    @Singular("subtypeMapping")
    private Map<String, TransactionSubtype> subtypeMappings;

    @Singular("sourceMapping")
    private Map<String, TransactionSource> sourceMappings;

    @Singular("ignoredDescription")
    @Size(max = 100)
    private List<String> ignoreDescriptions;

    @Valid
    @NotEmpty(message = "At least one responsibility assignment is required")
    @Builder.Default
    private List<ResponsibilityAllocation> responsibilities = new ArrayList<>();

    /**
     * Additional validation entry point executed once the final format is known.
     *
     * @param resolvedFormat the detected file format
     */
    public void validateFor(StatementImportFormat resolvedFormat) {
        ensureResponsibilitiesSumToOneHundred();
        if (resolvedFormat == StatementImportFormat.CSV && csv == null) {
            throw new IllegalArgumentException("CSV configuration is required to import CSV statements");
        }
        if (defaultCategoryId == null && (categoryMappings == null || categoryMappings.isEmpty())) {
            throw new IllegalArgumentException("A default category or category mappings must be provided");
        }
    }

    private void ensureResponsibilitiesSumToOneHundred() {
        BigDecimal total = responsibilities.stream()
                .map(ResponsibilityAllocation::normalizedPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("The sum of responsibility percentages must total 100%");
        }
    }

    /**
    * Returns the target zone id for the import.
    */
    public ZoneId resolveZoneId() {
        try {
            return timezone != null ? ZoneId.of(timezone) : ZoneId.systemDefault();
        } catch (Exception ex) {
            return ZoneId.systemDefault();
        }
    }

    public Map<String, Long> safeCategoryMappings() {
        return categoryMappings != null ? categoryMappings : Collections.emptyMap();
    }

    public Map<String, Long> safeSubcategoryMappings() {
        return subcategoryMappings != null ? subcategoryMappings : Collections.emptyMap();
    }

    public Map<String, Long> safeSourceEntityMappings() {
        return sourceEntityMappings != null ? sourceEntityMappings : Collections.emptyMap();
    }

    public Map<String, TransactionType> safeTypeMappings() {
        return typeMappings != null ? typeMappings : Collections.emptyMap();
    }

    public Map<String, TransactionSubtype> safeSubtypeMappings() {
        return subtypeMappings != null ? subtypeMappings : Collections.emptyMap();
    }

    public Map<String, TransactionSource> safeSourceMappings() {
        return sourceMappings != null ? sourceMappings : Collections.emptyMap();
    }

    public List<String> safeIgnoreDescriptions() {
        return ignoreDescriptions != null ? ignoreDescriptions : Collections.emptyList();
    }

    /**
     * Configuration block for CSV-based imports.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsvConfiguration {

        @Builder.Default
        private boolean containsHeader = true;

        @NotBlank
        @Pattern(regexp = "^.$", message = "Delimiter must be a single character")
        @Builder.Default
        private String delimiter = ";";

        /**
         * Optional override for decimal separator used in amounts. Defaults to locale settings.
         */
        @Pattern(regexp = "^.$", message = "Decimal separator must be a single character")
        private String decimalSeparator;

        /**
         * Optional override for grouping separator used in amounts. Defaults to locale settings.
         */
        @Pattern(regexp = "^.$", message = "Grouping separator must be a single character")
        private String groupingSeparator;

        @NotBlank
        @Builder.Default
        private String dateColumn = "date";

        @NotBlank
        @Builder.Default
        private String descriptionColumn = "description";

        @NotBlank
        @Builder.Default
        private String amountColumn = "amount";

        private String typeColumn;
        private String subtypeColumn;
        private String sourceColumn;
        private String categoryColumn;
        private String subcategoryColumn;
        private String sourceEntityColumn;
        private String externalIdColumn;

        @Builder.Default
        @Size(min = 1, message = "At least one date pattern must be provided")
        private List<String> datePatterns = new ArrayList<>(List.of("dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy"));

        /**
         * Locale tag used for parsing numbers and dates (defaults to pt-BR to match common Brazilian exports).
         */
        @NotBlank
        @Builder.Default
        private String locale = "pt-BR";

        @NotBlank
        @Builder.Default
        private String charset = StandardCharsets.UTF_8.name();

        public char resolveDelimiter() {
            return delimiter.charAt(0);
        }

        public char resolveDecimalSeparator() {
            if (decimalSeparator != null && !decimalSeparator.isBlank()) {
                return decimalSeparator.charAt(0);
            }
            return DecimalSymbolsDefaults.forLocale(resolveLocale()).decimalSeparator();
        }

        public char resolveGroupingSeparator() {
            if (groupingSeparator != null && !groupingSeparator.isBlank()) {
                return groupingSeparator.charAt(0);
            }
            return DecimalSymbolsDefaults.forLocale(resolveLocale()).groupingSeparator();
        }

        public Locale resolveLocale() {
            try {
                return Locale.forLanguageTag(locale);
            } catch (Exception ex) {
                return Locale.getDefault();
            }
        }

        public Charset resolveCharset() {
            try {
                return Charset.forName(charset);
            } catch (Exception ex) {
                return StandardCharsets.UTF_8;
            }
        }

        public List<String> safeDatePatterns() {
            return datePatterns != null && !datePatterns.isEmpty() ? datePatterns : List.of("dd/MM/yyyy");
        }

        @AssertTrue(message = "CSV import currently requires a header row to map columns")
        private boolean isHeaderSupported() {
            return containsHeader;
        }
    }

    /**
     * Responsible allocation definition reused for every imported transaction.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponsibilityAllocation {

        @NotNull
        private Long responsibleId;

        @NotNull
        private BigDecimal percentage;

        private String notes;

        private BigDecimal normalizedPercentage() {
            return percentage.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Helper record for locale defaults.
     */
    private record DecimalSymbolsDefaults(char decimalSeparator, char groupingSeparator) {
        private static DecimalSymbolsDefaults forLocale(Locale locale) {
            Locale effectiveLocale = locale != null ? locale : Locale.getDefault();
            Locale brazil = Locale.of("pt", "BR");
            char decimal = effectiveLocale.equals(brazil) ? ',' : '.';
            char grouping = effectiveLocale.equals(brazil) ? '.' : ',';
            return new DecimalSymbolsDefaults(decimal, grouping);
        }
    }

    @AssertTrue(message = "Timezone must be a valid ZoneId")
    private boolean isTimezoneValid() {
        try {
            ZoneId.of(timezone);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}

