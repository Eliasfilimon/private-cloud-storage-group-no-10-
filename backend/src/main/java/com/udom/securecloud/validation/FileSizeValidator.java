package com.udom.securecloud.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileSizeValidator: Validates file size for MultipartFile
 * Ensures file doesn't exceed maximum allowed size
 */
public class FileSizeValidator implements ConstraintValidator<ValidFileSize, MultipartFile> {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSizeValidator.class);
    private long maxSizeBytes;
    
    @Override
    public void initialize(ValidFileSize constraintAnnotation) {
        this.maxSizeBytes = constraintAnnotation.max() * 1024 * 1024; // Convert MB to bytes
    }
    
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull annotation
        if (file == null || file.isEmpty()) {
            return true;
        }
        
        long fileSize = file.getSize();
        
        if (fileSize > maxSizeBytes) {
            long maxSizeMB = maxSizeBytes / (1024 * 1024);
            logger.warn("File size {} bytes exceeds maximum {} MB", fileSize, maxSizeMB);
            
            addConstraintViolation(context, 
                String.format("File size must not exceed %d MB", maxSizeMB));
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
