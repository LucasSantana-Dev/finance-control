package com.finance_control.shared.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.finance_control.shared.exception.EntityMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for mapping between entities and DTOs using reflection.
 * Reduces boilerplate code in service classes.
 */
public class EntityMapper {

    private static final Logger log = LoggerFactory.getLogger(EntityMapper.class);

    // Cache for getter and setter methods to improve performance
    private static final Map<Class<?>, Map<String, Method>> GETTER_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Method>> SETTER_CACHE = new ConcurrentHashMap<>();

    private EntityMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Maps all common fields from source to target object.
     * Common fields are those with the same name and compatible types.
     *
     * @param source the source object
     * @param target the target object
     * @param <T>    the target type
     * @return the target object with mapped fields
     */
    public static <T> T mapCommonFields(Object source, T target) {
        if (source == null || target == null) {
            return target;
        }

        Map<String, Method> sourceGetters = getGetters(source.getClass());
        Map<String, Method> targetSetters = getSetters(target.getClass());

        for (Map.Entry<String, Method> entry : sourceGetters.entrySet()) {
            String fieldName = entry.getKey();
            Method getter = entry.getValue();

            Method setter = targetSetters.get(fieldName);
            if (setter != null && isCompatibleTypes(getter.getReturnType(), setter.getParameterTypes()[0])) {
                try {
                    Object value = getter.invoke(source);
                    // Only set non-null values to avoid overwriting with null
                    if (value != null) {
                        setter.invoke(target, value);
                    }
                } catch (Exception e) {
                    log.debug("Skipping field '{}' due to mapping error: {}", fieldName, e.getMessage());
                }
            }
        }

        return target;
    }

    /**
     * Maps specific fields from source to target object.
     *
     * @param source     the source object
     * @param target     the target object
     * @param fieldNames the names of fields to map
     * @param <T>        the target type
     * @return the target object with mapped fields
     */
    public static <T> T mapSpecificFields(Object source, T target, String... fieldNames) {
        if (source == null || target == null) {
            return target;
        }

        Map<String, Method> sourceGetters = getGetters(source.getClass());
        Map<String, Method> targetSetters = getSetters(target.getClass());

        for (String fieldName : fieldNames) {
            Method getter = sourceGetters.get(fieldName);
            Method setter = targetSetters.get(fieldName);

            if (getter != null && setter != null
                    && isCompatibleTypes(getter.getReturnType(), setter.getParameterTypes()[0])) {
                try {
                    Object value = getter.invoke(source);
                    setter.invoke(target, value);
                } catch (Exception e) {
                    log.debug("Skipping field '{}' due to mapping error: {}", fieldName, e.getMessage());
                }
            }
        }

        return target;
    }

    /**
     * Gets a map of getter methods for a class.
     * Uses caching to improve performance for repeated calls.
     *
     * @param clazz the class
     * @return a map of field names to getter methods
     */
    private static Map<String, Method> getGetters(Class<?> clazz) {
        return GETTER_CACHE.computeIfAbsent(clazz, EntityMapper::buildGettersMap);
    }

    /**
     * Builds the getters map for a class (uncached version).
     *
     * @param clazz the class
     * @return a map of field names to getter methods
     */
    private static Map<String, Method> buildGettersMap(Class<?> clazz) {
        Map<String, Method> getters = new HashMap<>();

        for (Method method : clazz.getMethods()) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0
                    && !method.getName().equals("getClass")) {
                String fieldName = method.getName().substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                getters.put(fieldName, method);
            }
        }

        return getters;
    }

    /**
     * Gets a map of setter methods for a class.
     * Uses caching to improve performance for repeated calls.
     *
     * @param clazz the class
     * @return a map of field names to setter methods
     */
    private static Map<String, Method> getSetters(Class<?> clazz) {
        return SETTER_CACHE.computeIfAbsent(clazz, EntityMapper::buildSettersMap);
    }

    /**
     * Builds the setters map for a class (uncached version).
     *
     * @param clazz the class
     * @return a map of field names to setter methods
     */
    private static Map<String, Method> buildSettersMap(Class<?> clazz) {
        Map<String, Method> setters = new HashMap<>();

        for (Method method : clazz.getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                String fieldName = method.getName().substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                setters.put(fieldName, method);
            }
        }

        return setters;
    }

    /**
     * Checks if two types are compatible for mapping.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return true if types are compatible
     */
    private static boolean isCompatibleTypes(Class<?> sourceType, Class<?> targetType) {
        if (sourceType.equals(targetType)) {
            return true;
        }

        // Handle primitive types
        if (sourceType.isPrimitive() && targetType.isPrimitive()) {
            return sourceType.equals(targetType);
        }

        // Handle primitive wrapper types
        if (sourceType.isPrimitive() && !targetType.isPrimitive()) {
            return getWrapperClass(sourceType).equals(targetType);
        }

        if (!sourceType.isPrimitive() && targetType.isPrimitive()) {
            return sourceType.equals(getWrapperClass(targetType));
        }

        // Handle inheritance
        return targetType.isAssignableFrom(sourceType);
    }

    /**
     * Gets the wrapper class for a primitive type.
     *
     * @param primitiveType the primitive type
     * @return the wrapper class
     */
    private static Class<?> getWrapperClass(Class<?> primitiveType) {
        if (primitiveType.equals(boolean.class)) {
            return Boolean.class;
        }
        if (primitiveType.equals(byte.class)) {
            return Byte.class;
        }
        if (primitiveType.equals(char.class)) {
            return Character.class;
        }
        if (primitiveType.equals(double.class)) {
            return Double.class;
        }
        if (primitiveType.equals(float.class)) {
            return Float.class;
        }
        if (primitiveType.equals(int.class)) {
            return Integer.class;
        }
        if (primitiveType.equals(long.class)) {
            return Long.class;
        }
        if (primitiveType.equals(short.class)) {
            return Short.class;
        }
        return primitiveType;
    }

    /**
     * Sets a field value using reflection.
     *
     * @param obj       the object
     * @param fieldName the field name
     * @param value     the value to set
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Method setter = getSetters(obj.getClass()).get(fieldName);
            if (setter != null) {
                setter.invoke(obj, value);
            }
        } catch (Exception e) {
            throw new EntityMappingException("Failed to set field " + fieldName, e);
        }
    }

    /**
     * Gets a field value using reflection.
     *
     * @param obj       the object
     * @param fieldName the field name
     * @return the field value
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Method getter = getGetters(obj.getClass()).get(fieldName);
            if (getter != null) {
                return getter.invoke(obj);
            }
            return null;
        } catch (Exception e) {
            throw new EntityMappingException("Failed to get field " + fieldName, e);
        }
    }

    /**
     * Clears the method cache. Useful for testing or when class definitions change.
     */
    public static void clearCache() {
        GETTER_CACHE.clear();
        SETTER_CACHE.clear();
    }
}
