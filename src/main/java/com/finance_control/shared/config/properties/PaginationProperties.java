package com.finance_control.shared.config.properties;

/**
 * Pagination configuration properties.
 */
public record PaginationProperties(
    int defaultPageSize,
    int maxPageSize,
    String defaultSort,
    String defaultDirection
) {
    public PaginationProperties() {
        this(10, 100, "id", "ASC");
    }
}

