package com.finance_control.shared.service.helper;

import com.finance_control.shared.context.UserContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * Helper class for building JPA Specifications in BaseService.
 * Extracted to reduce BaseService file length.
 *
 * @param <T> The entity type
 */
public class BaseServiceSpecificationBuilder<T> {

    private static final String USER_ID_FIELD = "userId";
    private static final String IS_ACTIVE_FIELD = "isActive";

    private final boolean userAware;

    public BaseServiceSpecificationBuilder(boolean userAware) {
        this.userAware = userAware;
    }

    /**
     * Creates a specification from search term and filters.
     *
     * @param search  the search term
     * @param filters the filters map
     * @return the specification
     */
    public Specification<T> createSpecificationFromFilters(String search, Map<String, Object> filters) {
        ensureUserFilter(filters);

        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            addSearchPredicates(root, criteriaBuilder, predicates, search);
            addFilterPredicates(root, criteriaBuilder, predicates, filters);

            return predicates.isEmpty() ? null
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void ensureUserFilter(Map<String, Object> filters) {
        if (!userAware) {
            return;
        }

        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User context not available");
        }

        if (filters != null && !filters.containsKey(USER_ID_FIELD)) {
            filters.put(USER_ID_FIELD, currentUserId);
        }
    }

    private void addSearchPredicates(Root<T> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            String search) {
        if (search != null && !search.trim().isEmpty()) {
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                            "%" + search.toLowerCase() + "%")));
        }
    }

    private void addFilterPredicates(Root<T> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Map<String, Object> filters) {
        if (filters != null) {
            filters.forEach((key, value) -> {
                if (value != null) {
                    addFilterPredicate(root, criteriaBuilder, predicates, key, value);
                }
            });
        }
    }

    private void addFilterPredicate(Root<T> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            String key, Object value) {
        switch (key) {
            case USER_ID_FIELD -> predicates.add(createUserIdPredicate(root, criteriaBuilder, value));
            case "name" -> predicates.add(createNamePredicate(root, criteriaBuilder, value));
            case IS_ACTIVE_FIELD -> predicates.add(createIsActivePredicate(root, criteriaBuilder, value));
            default -> {
                // Ignore unknown filter keys
            }
        }
    }

    private Predicate createUserIdPredicate(Root<T> root, CriteriaBuilder criteriaBuilder, Object value) {
        return criteriaBuilder.equal(root.get("user").get("id"), value);
    }

    private Predicate createNamePredicate(Root<T> root, CriteriaBuilder criteriaBuilder, Object value) {
        return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                "%" + value.toString().toLowerCase() + "%");
    }

    private Predicate createIsActivePredicate(Root<T> root, CriteriaBuilder criteriaBuilder, Object value) {
        return Boolean.TRUE.equals(value) ?
                criteriaBuilder.isTrue(root.get(IS_ACTIVE_FIELD))
                : criteriaBuilder.isFalse(root.get(IS_ACTIVE_FIELD));
    }
}

