package com.finance_control.shared.context;

/**
 * Thread-local context holder for the current authenticated user.
 * This class provides a way to access the current user's ID throughout
 * the request lifecycle without passing it as a parameter.
 */
public class UserContext {
    
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    
    /**
     * Sets the current user ID for the current thread.
     * 
     * @param userId the user ID to set
     */
    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }
    
    /**
     * Gets the current user ID for the current thread.
     * 
     * @return the current user ID, or null if not set
     */
    public static Long getCurrentUserId() {
        return currentUserId.get();
    }
    
    /**
     * Clears the current user ID for the current thread.
     * This should be called at the end of each request to prevent memory leaks.
     */
    public static void clear() {
        currentUserId.remove();
    }
    
    /**
     * Checks if a user ID is currently set for the current thread.
     * 
     * @return true if a user ID is set, false otherwise
     */
    public static boolean hasCurrentUserId() {
        return currentUserId.get() != null;
    }
} 