package com.finance_control.transactions.controller.helper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Helper class for creating Pageable instances with sorting in TransactionController.
 * Extracted to reduce class fan-out complexity.
 */
@Component
public class TransactionPageableHelper {

    /**
     * Creates a Pageable instance with sorting.
     *
     * @param page the page number
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDirection the sort direction
     * @return the Pageable instance with sorting
     */
    public Pageable createPageableWithSort(int page, int size, String sortBy, String sortDirection) {
        String effectiveSortBy = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : "createdAt";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, effectiveSortBy);
        return PageRequest.of(page, size, sort);
    }
}

