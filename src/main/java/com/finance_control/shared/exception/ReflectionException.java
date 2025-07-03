package com.finance_control.shared.exception;

/**
 * Exception thrown when reflection operations fail.
 */
public class ReflectionException extends RuntimeException {
    
    public ReflectionException(String message) {
        super(message);
    }
    
    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
} 