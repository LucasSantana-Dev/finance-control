package com.finance_control.shared.service.helper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Helper class for pageable operations in BaseService.
 * Extracted to reduce BaseService file length.
 */
public class BaseServicePageableHelper {

    /**
     * Creates a pageable with sorting.
     *
     * @param pageable      the base pageable
     * @param sortBy        the field to sort by
     * @param sortDirection the sort direction
     * @return the pageable with sorting
     */
    public static Pageable createPageableWithSort(Pageable pageable, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return pageable;
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(direction, sortBy);

        if (pageable.isPaged()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        } else {
            return PageRequest.of(0, Integer.MAX_VALUE, sort);
        }
    }
}

