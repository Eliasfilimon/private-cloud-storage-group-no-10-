package com.udom.securecloud.security.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * FileUploadValidator: Comprehensive file upload security validation.
 * 
 * Validation Layers:
 * 1. File extension whitelist (safe extensions only)
 * 2. File extension blacklist (dangerous extensions blocked)
 * 3. MIME type validation against declared extension
 * 4. Magic bytes/file signature verification (prevents disguised files)
 * 5. File size limits (prevents storage exhaustion)
 * 6. Filename sanitization (prevents path traversal attacks)
 */
@Service
public class FileUploadValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadValidator.class);
    
    // Whitelist of safe file extensions
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "pdf", "docx", "doc", "xlsx", "xls", "pptx", "ppt",
        "txt", "csv", "jpg", "jpeg", "png", "gif", "webp",
        "zip", "rar", "7z", "mp4", "avi", "mov"
    ));
    
    // Blacklist of dangerous extensions
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
        "exe", "sh", "bat", "cmd", "com", "pif", "scr", "vbs", "js",
        "jar", "class", "dll", "sys", "app", "deb", "rpm", "msi", "dmg"
    ));
    
    // Maximum file size: 100 MB/hapa nimeongeza mpaka kua 500 mb
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    
    // Magic bytes for file signature verification
    private static final Map<String, byte[][]> FILE_SIGNATURES = new HashMap<>();
    
    static {
        // PDF signature
        FILE_SIGNATURES.put("pdf", new byte[][] {{(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46}});
        
        // ZIP signature (also used by docx, xlsx, pptx)
        FILE_SIGNATURES.put("zip", new byte[][] {{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04}});
        FILE_SIGNATURES.put("docx", new byte[][] {{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04}});
        FILE_SIGNATURES.put("xlsx", new byte[][] {{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04}});
        FILE_SIGNATURES.put("pptx", new byte[][] {{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04}});
        
        // JPEG signature
        FILE_SIGNATURES.put("jpg", new byte[][] {{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}});
        FILE_SIGNATURES.put("jpeg", new byte[][] {{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}});
        
        // PNG signature
        FILE_SIGNATURES.put("png", new byte[][] {{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47}});
        
        // GIF signature
        FILE_SIGNATURES.put("gif", new byte[][] {
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x37, (byte) 0x61},
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61}
        });
    }
    
    /**
     * Validate file upload before saving
     * @param file The uploaded file
     * @param userId The user uploading the file
     * @throws FileUploadValidationException if any validation fails
     */
    public void validateFile(MultipartFile file, String userId) throws FileUploadValidationException {
        logger.debug("Validating file upload from user: {}", userId);
        
        // Check file not empty
        if (file.isEmpty()) {
            logger.warn("Empty file upload attempted by user: {}", userId);
            throw new FileUploadValidationException("File cannot be empty");
        }
        
        // Validate filename
        String filename = file.getOriginalFilename();
        validateFilename(filename);
        
        // Extract extension
        String extension = getFileExtension(filename).toLowerCase();
        
        // Validate extension
        validateExtension(extension);
        
        // Validate file size
        validateFileSize(file.getSize());
        
        // Validate MIME type
        validateMimeType(file.getContentType(), extension);
        
        // Validate file signature (magic bytes)
        try {
            validateFileSignature(file.getBytes(), extension);
        } catch (IOException e) {
            logger.error("Error reading file for signature validation: {}", e.getMessage());
            throw new FileUploadValidationException("Unable to validate file contents");
        }
        
        logger.info("File validation successful for user: {} - File: {}", userId, filename);
    }
    
    /**
     * Validate filename for path traversal and suspicious patterns
     */
    private void validateFilename(String filename) throws FileUploadValidationException {
        if (filename == null || filename.isEmpty()) {
            throw new FileUploadValidationException("Filename cannot be empty");
        }
        
        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            logger.warn("Path traversal attempt detected in filename: {}", filename);
            throw new FileUploadValidationException("Invalid filename - path traversal detected");
        }
        
        // Check filename length
        if (filename.length() > 255) {
            throw new FileUploadValidationException("Filename too long (max 255 characters)");
        }
    }
    
    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * Validate file extension against whitelist and blacklist
     */
    private void validateExtension(String extension) throws FileUploadValidationException {
        if (extension.isEmpty()) {
            throw new FileUploadValidationException("File must have an extension");
        }
        
        // Check blacklist first
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            logger.warn("Dangerous file extension blocked: {}", extension);
            throw new FileUploadValidationException("File type not allowed: ." + extension);
        }
        
        // Check whitelist
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            logger.warn("File extension not whitelisted: {}", extension);
            throw new FileUploadValidationException("File type not allowed: ." + extension);
        }
    }
    
    /**
     * Validate file size
     */
    private void validateFileSize(long fileSize) throws FileUploadValidationException {
        if (fileSize > MAX_FILE_SIZE) {
            throw new FileUploadValidationException(
                "File too large - max size is 100 MB (file size: " + (fileSize / 1024 / 1024) + " MB)"
            );
        }
    }
    
    /**
     * Validate MIME type against declared extension
     */
    private void validateMimeType(String contentType, String extension) throws FileUploadValidationException {
        if (contentType == null || contentType.isEmpty()) {
            return; // Skip MIME validation if not provided
        }
        
        // Basic MIME type validation
        String expectedMimeType = getExpectedMimeType(extension);
        if (expectedMimeType != null && !contentType.startsWith(expectedMimeType.split("/")[0])) {
            logger.warn("MIME type mismatch - declared: {}, extension: {}", contentType, extension);
            throw new FileUploadValidationException("MIME type does not match file extension");
        }
    }
    
    /**
     * Get expected MIME type for extension
     */
    private String getExpectedMimeType(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "docx", "doc" -> "application/";
            case "xlsx", "xls" -> "application/";
            case "pptx", "ppt" -> "application/";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "zip", "rar", "7z" -> "application/";
            case "txt" -> "text/";
            case "csv" -> "text/csv";
            case "mp4" -> "video/mp4";
            default -> null;
        };
    }
    
    /**
     * Validate file signature (magic bytes) to prevent disguised files
     */
    private void validateFileSignature(byte[] fileBytes, String extension) throws FileUploadValidationException {
        byte[][] signatures = FILE_SIGNATURES.get(extension);
        
        if (signatures == null) {
            // No signature check available for this extension
            return;
        }
        
        for (byte[] signature : signatures) {
            if (fileBytes.length < signature.length) {
                continue;
            }
            
            boolean matches = true;
            for (int i = 0; i < signature.length; i++) {
                if (fileBytes[i] != signature[i]) {
                    matches = false;
                    break;
                }
            }
            
            if (matches) {
                logger.debug("File signature validated for extension: {}", extension);
                return;
            }
        }
        
        logger.warn("File signature validation failed for extension: {}", extension);
        throw new FileUploadValidationException("File signature does not match declared format");
    }
}
