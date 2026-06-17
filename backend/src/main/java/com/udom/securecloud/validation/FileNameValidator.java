package com.udom.securecloud.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileNameValidator: Validates file names for security issues
 * - Prevents path traversal attacks (.., /, \)
 * - Prevents reserved names (CON, PRN, AUX, etc.)
 * - Validates length and allowed characters
 */
public class FileNameValidator implements ConstraintValidator<ValidFileName, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(FileNameValidator.class);
    
    @Override
    public void initialize(ValidFileName constraintAnnotation) {
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
            addConstraintViolation(context, "File name cannot be empty");
            return false;
        }
        
        // Check length
        if (value.length() < ValidationConstants.MIN_FILE_NAME_LENGTH) {
            addConstraintViolation(context, 
                "File name must be at least " + ValidationConstants.MIN_FILE_NAME_LENGTH + " character");
            return false;
        }
        
        if (value.length() > ValidationConstants.MAX_FILE_NAME_LENGTH) {
            addConstraintViolation(context, 
                "File name must not exceed " + ValidationConstants.MAX_FILE_NAME_LENGTH + " characters");
            return false;
        }
        
        // Check for path traversal attempts
        if (value.contains("..") || value.contains("/") || value.contains("\\")) {
            logger.warn("Path traversal attempt detected in file name: {}", value);
            addConstraintViolation(context, ValidationConstants.PATH_TRAVERSAL_MESSAGE);
            return false;
        }
        
        // Check for null bytes
        if (value.contains("\0")) {
            logger.warn("Null byte detected in file name: {}", value);
            addConstraintViolation(context, "File name contains invalid null byte");
            return false;
        }
        
        // Check for reserved names (Windows)
        String upperName = value.toUpperCase().replaceAll("\\.[^.]*$", ""); // Remove extension
        for (String reserved : ValidationConstants.RESERVED_NAMES) {
            if (upperName.equals(reserved)) {
                logger.warn("Reserved file name detected: {}", value);
                addConstraintViolation(context, "File name '" + value + "' is reserved and not allowed");
                return false;
            }
        }
        
        // Check for invalid characters
        if (!value.matches(ValidationConstants.FILE_NAME_PATTERN)) {
            addConstraintViolation(context, ValidationConstants.FILE_NAME_MESSAGE);
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
