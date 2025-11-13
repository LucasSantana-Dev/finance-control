package com.finance_control.transactions.importer;

/**
 * Classification for issues detected while importing statements.
 */
public enum TransactionImportIssueType {
    /**
     * Error encountered while parsing or validating a line/entry.
     */
    PARSING_ERROR,

    /**
     * Entry skipped due to an already existing transaction.
     */
    DUPLICATE_SKIPPED,

    /**
     * Entry skipped because configuration constraints could not be satisfied.
     */
    CONFIGURATION_REJECTED
}

