package com.finance_control.transactions.dto.importer;

import com.finance_control.transactions.dto.TransactionDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Response payload describing the outcome of a transaction statement import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionImportResponse {

    /**
     * Total number of entries parsed from the statement.
     */
    private int totalEntries;

    /**
     * Number of entries that passed validation (including dry-run scenarios).
     */
    private int processedEntries;

    /**
     * Number of transactions that were persisted (0 when running in dry-run mode).
     */
    private int createdTransactions;

    /**
     * Number of entries skipped due to duplicate detection.
     */
    private int duplicateEntries;

    /**
     * Indicates whether the operation executed without persisting changes.
     */
    private boolean dryRun;

    /**
     * Subset of transactions that were created during the import.
     */
    @Singular("createdTransaction")
    private List<TransactionDTO> createdTransactionSummaries;

    /**
     * Detailed issues detected while processing the statement.
     */
    @Singular
    private List<TransactionImportIssueDTO> issues;
}

