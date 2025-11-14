package com.finance_control.open_finance.exception;

/**
 * Exception thrown when Open Finance API calls fail.
 */
public class OpenFinanceApiException extends RuntimeException {

    private final int statusCode;

    public OpenFinanceApiException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public OpenFinanceApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public OpenFinanceApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public OpenFinanceApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
