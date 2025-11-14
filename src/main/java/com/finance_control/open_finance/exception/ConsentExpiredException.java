package com.finance_control.open_finance.exception;

/**
 * Exception thrown when a consent has expired.
 */
public class ConsentExpiredException extends RuntimeException {

    public ConsentExpiredException(String message) {
        super(message);
    }

    public ConsentExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
