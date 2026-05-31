package com.udom.securecloud.security.validation;

/**
 * FileUploadValidationException: Exception thrown when file upload validation fails
 */
public class FileUploadValidationException extends Exception {
    
    public FileUploadValidationException(String message) {
        super(message);
    }
    
    public FileUploadValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
