package com.finance_control.transactions.service;

import com.finance_control.transactions.model.Transaction;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * Helper class for building JPA Specifications for Transaction queries.
 * Reduces complexity and coupling in TransactionService.
 */
@Component
@RequiredArgsConstructor
public class TransactionSpecificationBuilder {

    private static final String FIELD_DESCRIPTION = "description";

    public Specification<Transaction> buildSpecification(String search, Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();

            addSearchPredicate(predicates, search, root, criteriaBuilder);
            addFilterPredicates(predicates, filters, root, criteriaBuilder);

            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addSearchPredicate(ArrayList<Predicate> predicates, String search,
            jakarta.persistence.criteria.Root<Transaction> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        if (search != null && !search.trim().isEmpty()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_DESCRIPTION)),
                    "%" + search.toLowerCase() + "%"));
        }
    }

    private void addFilterPredicates(ArrayList<Predicate> predicates, Map<String, Object> filters,
            jakarta.persistence.criteria.Root<Transaction> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        if (filters == null) {
            return;
        }

        filters.forEach((key, value) -> {
            if (value != null) {
                addFilterPredicate(predicates, key, value, root, criteriaBuilder);
            }
        });
    }

    private void addFilterPredicate(ArrayList<Predicate> predicates, String key, Object value,
            jakarta.persistence.criteria.Root<Transaction> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        switch (key) {
            case "userId" -> predicates.add(criteriaBuilder.equal(root.get("user").get("id"), value));
            case "type" -> predicates.add(criteriaBuilder.equal(root.get("type"), value));
            case "categoryId" -> predicates.add(criteriaBuilder.equal(root.get("category").get("id"), value));
            case "subcategoryId" -> predicates.add(criteriaBuilder.equal(root.get("subcategory").get("id"), value));
            case "sourceEntityId" -> predicates.add(criteriaBuilder.equal(root.get("sourceEntity").get("id"), value));
            case FIELD_DESCRIPTION ->
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_DESCRIPTION)),
                        "%" + value.toString().toLowerCase() + "%"));
            default -> {
                // Ignore unknown filter keys
            }
        }
    }
}



