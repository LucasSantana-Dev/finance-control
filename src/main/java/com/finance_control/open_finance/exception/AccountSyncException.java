package com.finance_control.open_finance.exception;

/**
 * Exception thrown when account synchronization fails.
 */
public class AccountSyncException extends RuntimeException {

    public AccountSyncException(String message) {
        super(message);
    }

    public AccountSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
