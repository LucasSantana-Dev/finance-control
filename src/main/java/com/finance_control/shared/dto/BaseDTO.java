package com.finance_control.shared.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Base DTO class providing common fields for all DTOs.
 * This abstract class defines standard fields like id, createdAt, and updatedAt
 * that are common across all entities in the system.
 * 
 * <p><strong>Usage Guidelines:</strong>
 * <ul>
 *   <li>All response DTOs should extend this class</li>
 *   <li>Use {@code @EqualsAndHashCode(callSuper = true)} in subclasses</li>
 *   <li>ID is optional for creation, required for updates and responses</li>
 *   <li>Audit fields are automatically populated by the system</li>
 * </ul>
 * </p>
 * 
 * @param <I> The ID type of the DTO (typically Long)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public abstract class BaseDTO<I> {
    
    /** The unique identifier of the entity */
    private I id;
    
    /** The timestamp when the entity was created */
    private LocalDateTime createdAt;
    
    /** The timestamp when the entity was last updated */
    private LocalDateTime updatedAt;
    
    /**
     * Validates the DTO for create operations.
     * Template method that can be overridden by subclasses to add specific validation logic.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateCreate() {
        // Default implementation - override in subclasses for specific validation
    }
    
    /**
     * Validates the DTO for update operations.
     * Template method that can be overridden by subclasses to add specific validation logic.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateUpdate() {
        // Default implementation - override in subclasses for specific validation
    }
    
    /**
     * Validates that the DTO is properly populated for response operations.
     * Ensures the ID is present, which is required for all response operations.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateResponse() {
        if (id == null) {
            throw new IllegalArgumentException("ID is required for response");
        }
    }
} 