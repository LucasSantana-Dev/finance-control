package com.finance_control.transactions.dto.importer;

import com.finance_control.transactions.importer.TransactionImportIssueType;
import lombok.Builder;
import lombok.Value;

/**
 * DTO describing an issue detected while processing an imported statement entry.
 */
@Value
@Builder
public class TransactionImportIssueDTO {

    /**
     * Sequential position of the entry within the imported file (1-based).
     */
    int lineNumber;

    /**
     * External identifier or bank reference when available.
     */
    String externalReference;

    /**
     * Short human-readable message describing the issue.
     */
    String message;

    /**
     * Category classification for the issue.
     */
    TransactionImportIssueType type;
}

