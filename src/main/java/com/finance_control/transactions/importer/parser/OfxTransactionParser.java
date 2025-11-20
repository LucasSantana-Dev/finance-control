package com.finance_control.transactions.importer.parser;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.importer.TransactionImportIssueDTO;
import com.finance_control.transactions.dto.importer.TransactionImportRequest;
import com.finance_control.transactions.importer.parser.helper.OfxTransactionExtractor;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.DefaultStringConversion;
import com.webcohesion.ofx4j.io.OFXParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for OFX (Open Financial Exchange) statement files.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfxTransactionParser {

    private final OfxTransactionExtractor transactionExtractor = new OfxTransactionExtractor();

    public ParseResult parseOfx(MultipartFile file, TransactionImportRequest request) {
        List<ImportedEntry> entries = new ArrayList<>();
        List<TransactionImportIssueDTO> issues = new ArrayList<>();
        try (var input = file.getInputStream();
             var reader = new InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8)) {

            var stringConversion = new DefaultStringConversion(request.resolveZoneId().getId());
            AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<>(ResponseEnvelope.class);
            unmarshaller.setConversion(stringConversion);

            ResponseEnvelope envelope = unmarshaller.unmarshal(reader);

            transactionExtractor.extractAllTransactions(envelope, request, entries);
            return new ParseResult(entries, issues);
        } catch (IOException | OFXParseException ex) {
            throw new IllegalArgumentException("Unable to parse OFX file", ex);
        }
    }


    public record ImportedEntry(int lineNumber,
                                String externalId,
                                LocalDateTime date,
                                String description,
                                BigDecimal amount,
                                TransactionType type,
                                String subtypeValue,
                                TransactionSource source,
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
