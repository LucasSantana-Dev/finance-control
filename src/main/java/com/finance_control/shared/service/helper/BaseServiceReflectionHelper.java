package com.finance_control.shared.service.helper;

import com.finance_control.shared.exception.ReflectionException;

import java.lang.reflect.Method;

/**
 * Helper class for reflection utilities in BaseService.
 * Extracted to reduce BaseService file length.
 */
public class BaseServiceReflectionHelper {

    /**
     * Gets the name from a DTO using reflection.
     *
     * @param dto the DTO
     * @return the name
     */
    public String getNameFromDTO(Object dto) {
        return getFieldValue(dto, "getName", String.class);
    }

    /**
     * Generic method to get a field value using reflection.
     *
     * @param obj        the object to get the field from
     * @param methodName the getter method name
     * @param returnType the expected return type
     * @return the field value
     */
    @SuppressWarnings("unchecked")
    public <V> V getFieldValue(Object obj, String methodName, Class<V> returnType) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            return (V) method.invoke(obj);
        } catch (Exception e) {
            throw new ReflectionException("Failed to get field value using " + methodName, e);
        }
    }
}

