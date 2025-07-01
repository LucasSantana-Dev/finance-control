package com.finance_control.shared.exception;

public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s not found with id: %d", entityName, id));
    }
    
    public EntityNotFoundException(String entityName, String field, Object value) {
        super(String.format("%s not found with %s: %s", entityName, field, value));
    }
} 