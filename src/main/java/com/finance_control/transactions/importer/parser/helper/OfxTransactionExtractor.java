package com.finance_control.transactions.importer.parser.helper;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.importer.TransactionImportRequest;
import com.finance_control.transactions.importer.parser.OfxTransactionParser;
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponse;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.banking.BankingResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.common.StatementResponse;
import com.webcohesion.ofx4j.domain.data.common.TransactionList;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponse;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardResponseMessageSet;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Helper class for extracting transactions from OFX response envelope.
 * Extracted from OfxTransactionParser to reduce class fan-out complexity.
 */
public class OfxTransactionExtractor {

    /**
     * Extracts all transactions from the OFX response envelope.
     *
     * @param envelope the OFX response envelope
     * @param request the import request
     * @param entries the list to add entries to
     * @return the final index after extraction
     */
    public int extractAllTransactions(ResponseEnvelope envelope, TransactionImportRequest request,
                                      List<OfxTransactionParser.ImportedEntry> entries) {
        int index = 0;
        index = extractBankingTransactions(envelope, request, entries, index);
        index = extractCreditCardTransactions(envelope, request, entries, index);
        return index;
    }

    private int extractBankingTransactions(ResponseEnvelope envelope, TransactionImportRequest request,
                                          List<OfxTransactionParser.ImportedEntry> entries, int startingIndex) {
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

    private int extractCreditCardTransactions(ResponseEnvelope envelope, TransactionImportRequest request,
                                              List<OfxTransactionParser.ImportedEntry> entries, int startingIndex) {
        ResponseMessageSet messageSet = envelope.getMessageSet(MessageSetType.creditcard);
        if (!(messageSet instanceof CreditCardResponseMessageSet creditCardResponse)) {
            return startingIndex;
        }

        int index = startingIndex;
        for (CreditCardStatementResponseTransaction transaction : creditCardResponse.getStatementResponses()) {
            CreditCardStatementResponse message = transaction.getMessage();
            index = extractStatementTransactions(entries, request, index, message, TransactionSource.CREDIT_CARD);
        }
        return index;
    }

    private int extractStatementTransactions(List<OfxTransactionParser.ImportedEntry> entries,
                                             TransactionImportRequest request,
                                             int index,
                                             StatementResponse response,
                                             TransactionSource fallbackSource) {
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

            entries.add(new OfxTransactionParser.ImportedEntry(index,
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

    private static String sanitize(String value) {
        return value != null ? value.trim() : null;
    }
}

