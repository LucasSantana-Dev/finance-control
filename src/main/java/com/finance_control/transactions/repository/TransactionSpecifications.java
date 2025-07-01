package com.finance_control.transactions.repository;

import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.util.SpecificationUtils;
import com.finance_control.transactions.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Specifications for dynamic Transaction filtering.
 * Uses the generic SpecificationUtils for common operations.
 */
public class TransactionSpecifications {
    
    /**
     * Creates a specification for filtering by user ID.
     */
    public static Specification<Transaction> hasUserId(Long userId) {
        return SpecificationUtils.fieldEqualNested("user", "id", userId);
    }
    
    /**
     * Creates a specification for filtering by transaction type.
     */
    public static Specification<Transaction> hasType(TransactionType type) {
        return SpecificationUtils.fieldEqual("type", type);
    }
    
    /**
     * Creates a specification for filtering by category ID.
     */
    public static Specification<Transaction> hasCategoryId(Long categoryId) {
        return SpecificationUtils.fieldEqualNested("category", "id", categoryId);
    }
    
    /**
     * Creates a specification for filtering by subcategory ID.
     */
    public static Specification<Transaction> hasSubcategoryId(Long subcategoryId) {
        return SpecificationUtils.fieldEqualNested("subcategory", "id", subcategoryId);
    }
    
    /**
     * Creates a specification for filtering by source entity ID.
     */
    public static Specification<Transaction> hasSourceEntityId(Long sourceEntityId) {
        return SpecificationUtils.fieldEqualNested("sourceEntity", "id", sourceEntityId);
    }
    
    /**
     * Creates a specification for filtering by date range.
     */
    public static Specification<Transaction> hasDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return SpecificationUtils.dateBetween("date", startDate, endDate);
    }
    
    /**
     * Creates a specification for filtering by description (case-insensitive).
     */
    public static Specification<Transaction> hasDescriptionLike(String description) {
        return SpecificationUtils.likeIgnoreCase("description", description);
    }
    
    /**
     * Creates a specification for filtering by amount range.
     */
    public static Specification<Transaction> hasAmountBetween(Double minAmount, Double maxAmount) {
        return SpecificationUtils.numberBetween("amount", minAmount, maxAmount);
    }
    
    /**
     * Creates a specification for filtering by responsible ID.
     */
    public static Specification<Transaction> hasResponsibleId(Long responsibleId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.join("responsibilities").get("responsible").get("id"), responsibleId);
    }
} 