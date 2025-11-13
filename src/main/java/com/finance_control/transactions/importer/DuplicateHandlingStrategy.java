package com.finance_control.transactions.importer;

/**
 * Strategy defining how to handle detected duplicate transactions during imports.
 */
public enum DuplicateHandlingStrategy {
    /**
     * Skip duplicates and keep existing transactions untouched.
     */
    SKIP,

    /**
     * Allow duplicates to be created alongside existing transactions.
     */
    ALLOW
}

