package com.finance_control.shared.service.helper;

import com.finance_control.shared.util.ValidationUtils;

/**
 * Helper class for validation logic in BaseService.
 * Extracted to reduce BaseService file length.
 */
public class BaseServiceValidationHelper {

    /**
     * Validates an ID value.
     *
     * @param id the ID to validate
     * @throws IllegalArgumentException if the ID is invalid
     */
    public static <I> void validateId(I id) {
        if (id instanceof Long longId) {
            ValidationUtils.validateId(longId);
        } else if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }
}
