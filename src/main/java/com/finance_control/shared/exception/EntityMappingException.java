package com.finance_control.shared.exception;

/**
 * Exception thrown when entity mapping operations fail.
 */
public class EntityMappingException extends RuntimeException {
    
    public EntityMappingException(String message) {
        super(message);
    }
    
    public EntityMappingException(String message, Throwable cause) {
        super(message, cause);
    }
} 