package com.udom.securecloud.security.authorization;

/**
 * UnauthorizedException: Exception thrown when authorization check fails.
 * Tracks user and resource IDs for security auditing.
 */
public class UnauthorizedException extends Exception {
    
    private final String userId;
    private final Long resourceId;
    
    public UnauthorizedException(String message, String userId, Long resourceId) {
        super(message);
        this.userId = userId;
        this.resourceId = resourceId;
    }
    
    public UnauthorizedException(String message, String userId, Long resourceId, Throwable cause) {
        super(message, cause);
        this.userId = userId;
        this.resourceId = resourceId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
}
