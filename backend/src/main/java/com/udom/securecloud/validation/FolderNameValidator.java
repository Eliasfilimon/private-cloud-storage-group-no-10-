package com.udom.securecloud.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FolderNameValidator: Validates folder names for security issues
 * - Prevents path traversal attacks (.., /, \)
 * - Prevents reserved names (CON, PRN, AUX, etc.)
 * - Validates length and allowed characters
 */
public class FolderNameValidator implements ConstraintValidator<ValidFolderName, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(FolderNameValidator.class);
    
    @Override
    public void initialize(ValidFolderName constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull annotation
        if (value == null) {
            return true;
        }
        
        // Check if empty
        if (value.trim().isEmpty()) {
            addConstraintViolation(context, "Folder name cannot be empty");
            return false;
        }
        
        // Check length
        if (value.length() < ValidationConstants.MIN_FOLDER_NAME_LENGTH) {
            addConstraintViolation(context, 
                "Folder name must be at least " + ValidationConstants.MIN_FOLDER_NAME_LENGTH + " character");
            return false;
        }
        
        if (value.length() > ValidationConstants.MAX_FOLDER_NAME_LENGTH) {
            addConstraintViolation(context, 
                "Folder name must not exceed " + ValidationConstants.MAX_FOLDER_NAME_LENGTH + " characters");
            return false;
        }
        
        // Check for path traversal attempts
        if (value.contains("..") || value.contains("/") || value.contains("\\")) {
            logger.warn("Path traversal attempt detected in folder name: {}", value);
            addConstraintViolation(context, ValidationConstants.PATH_TRAVERSAL_MESSAGE);
            return false;
        }
        
        // Check for null bytes
        if (value.contains("\0")) {
            logger.warn("Null byte detected in folder name: {}", value);
            addConstraintViolation(context, "Folder name contains invalid null byte");
            return false;
        }
        
        // Check for reserved names (Windows)
        String upperName = value.toUpperCase();
        for (String reserved : ValidationConstants.RESERVED_NAMES) {
            if (upperName.equals(reserved)) {
                logger.warn("Reserved folder name detected: {}", value);
                addConstraintViolation(context, "Folder name '" + value + "' is reserved and not allowed");
                return false;
            }
        }
        
        // Check for invalid characters
        if (!value.matches(ValidationConstants.FOLDER_NAME_PATTERN)) {
            addConstraintViolation(context, ValidationConstants.FOLDER_NAME_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}
