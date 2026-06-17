package com.udom.securecloud.validation;

/**
 * ValidationConstants: Centralized validation patterns and limits
 * Used across all validators and DTOs for consistency
 */
public class ValidationConstants {
    
    // ==================== EMAIL ====================
    public static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    public static final String EMAIL_MESSAGE = "Invalid email format";
    
    // ==================== USERNAME ====================
    public static final String USERNAME_PATTERN = 
        "^[a-zA-Z0-9_-]{3,50}$";
    public static final String USERNAME_MESSAGE = 
        "Username must be 3-50 characters, alphanumeric with underscore/dash only";
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    
    // ==================== PASSWORD ====================
    public static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-+=.#^~])[A-Za-z\\d@$!%*?&_\\-+=.#^~]{12,}$";
    public static final String PASSWORD_MESSAGE = 
        "Password must be 12+ characters with uppercase, lowercase, digit, and special character";
    public static final int MIN_PASSWORD_LENGTH = 12;
    
    // ==================== FILE/FOLDER NAMES ====================
    public static final String FILE_NAME_PATTERN = 
        "^[a-zA-Z0-9\\s\\-._()&]+$";
    public static final String FILE_NAME_MESSAGE = 
        "File name contains invalid characters. Allowed: alphanumeric, space, dash, underscore, dot, parentheses, ampersand";
    public static final int MAX_FILE_NAME_LENGTH = 255;
    public static final int MIN_FILE_NAME_LENGTH = 1;
    
    public static final String FOLDER_NAME_PATTERN = 
        "^[a-zA-Z0-9\\s\\-._()&]+$";
    public static final String FOLDER_NAME_MESSAGE = 
        "Folder name contains invalid characters. Allowed: alphanumeric, space, dash, underscore, dot, parentheses, ampersand";
    public static final int MAX_FOLDER_NAME_LENGTH = 255;
    public static final int MIN_FOLDER_NAME_LENGTH = 1;
    
    // Reserved folder names (Windows + Unix)
    public static final String[] RESERVED_NAMES = {
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
        ".", "..", "~", "$RECYCLE.BIN", "System Volume Information"
    };
    
    // ==================== SEARCH QUERY ====================
    public static final String SEARCH_PATTERN = 
        "^[a-zA-Z0-9\\s\\-._]+$";
    public static final String SEARCH_MESSAGE = 
        "Search query contains invalid characters";
    public static final int MIN_SEARCH_LENGTH = 1;
    public static final int MAX_SEARCH_LENGTH = 100;
    
    // ==================== ROLE ====================
    public static final String ROLE_PATTERN = 
        "^(ADMIN|STAFF)$";
    public static final String ROLE_MESSAGE = 
        "Role must be ADMIN or STAFF";
    
    // ==================== PERMISSION ====================
    public static final String PERMISSION_PATTERN = 
        "^(VIEW|EDIT|DOWNLOAD)$";
    public static final String PERMISSION_MESSAGE = 
        "Permission must be VIEW, EDIT, or DOWNLOAD";
    
    // ==================== UUID ====================
    public static final String UUID_PATTERN = 
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    public static final String UUID_MESSAGE = 
        "Invalid UUID format";
    
    // ==================== TOKEN ====================
    public static final int MIN_TOKEN_LENGTH = 100;
    public static final int MAX_TOKEN_LENGTH = 2000;
    public static final String TOKEN_MESSAGE = 
        "Invalid token format";
    
    // ==================== FILE SIZE LIMITS ====================
    public static final long MAX_FILE_SIZE_MB = 500;
    public static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    public static final String FILE_SIZE_MESSAGE = 
        "File size must not exceed 500MB";
    
    // ==================== REQUEST SIZE LIMITS ====================
    public static final long MAX_REQUEST_SIZE_MB = 600;
    public static final long MAX_REQUEST_SIZE_BYTES = MAX_REQUEST_SIZE_MB * 1024 * 1024;
    public static final String REQUEST_SIZE_MESSAGE = 
        "Request size must not exceed 600MB";
    
    // ==================== STORAGE QUOTA ====================
    public static final int MIN_QUOTA_GB = 1;
    public static final int MAX_QUOTA_GB = 1000;
    public static final String QUOTA_MESSAGE = 
        "Storage quota must be between 1GB and 1000GB";
    
    // ==================== DEPARTMENT ====================
    public static final int MAX_DEPARTMENT_LENGTH = 100;
    public static final String DEPARTMENT_MESSAGE = 
        "Department name must not exceed 100 characters";
    
    // ==================== REASON/NOTES ====================
    public static final int MAX_REASON_LENGTH = 1000;
    public static final int MIN_REASON_LENGTH = 10;
    public static final String REASON_MESSAGE = 
        "Reason must be between 10 and 1000 characters";
    
    // ==================== DATE RANGE ====================
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_MESSAGE = 
        "Invalid date format. Use: yyyy-MM-dd'T'HH:mm:ss";
    
    // ==================== PAGINATION ====================
    public static final int MIN_PAGE = 0;
    public static final int MAX_PAGE = 10000;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    // ==================== BATCH OPERATIONS ====================
    public static final int MAX_BATCH_SIZE = 100;
    public static final String BATCH_SIZE_MESSAGE = 
        "Batch size must not exceed 100 items";
    
    // ==================== VALIDATION MESSAGES ====================
    public static final String REQUIRED_MESSAGE = "This field is required";
    public static final String INVALID_FORMAT_MESSAGE = "Invalid format";
    public static final String PATH_TRAVERSAL_MESSAGE = 
        "Path traversal detected. Names cannot contain '..' or path separators";
    
    // Private constructor to prevent instantiation
    private ValidationConstants() {
        throw new AssertionError("Cannot instantiate ValidationConstants");
    }
}
