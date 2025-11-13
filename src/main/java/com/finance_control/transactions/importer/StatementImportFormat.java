package com.finance_control.transactions.importer;

/**
 * Supported formats for transaction statement imports.
 */
public enum StatementImportFormat {
    /**
     * Detect format automatically based on file metadata.
     */
    AUTO,

    /**
     * Standard comma-separated values format.
     */
    CSV,

    /**
     * Open Financial Exchange format.
     */
    OFX
}

