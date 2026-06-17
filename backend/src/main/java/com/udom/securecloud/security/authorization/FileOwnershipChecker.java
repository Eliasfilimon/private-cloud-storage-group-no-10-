package com.udom.securecloud.security.authorization;

import com.udom.securecloud.model.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FileOwnershipChecker: IDOR (Insecure Direct Object Reference) prevention.
 * 
 * Ensures users can only:
 * - Download their own files
 * - Delete their own files
 * - Share their own files
 * 
 * This provides server-side authorization checks that cannot be bypassed
 * by manipulating client-side parameters.
 */
@Service
public class FileOwnershipChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(FileOwnershipChecker.class);
    
    /**
     * Check file ownership and operation authorization
     * @param file The file being accessed
     * @param userId The user attempting the operation
     * @param operation The operation being performed (DOWNLOAD, DELETE, SHARE)
     * @throws UnauthorizedException if user doesn't own the file
     */
    public void checkOwnership(FileMetadata file, String userId, String operation) throws UnauthorizedException {
        if (file == null) {
            logger.warn("File is null for operation: {} by user: {}", operation, userId);
            throw new UnauthorizedException("File not found", userId, null);
        }
        
        String fileOwnerId = file.getUser().getUsername();
        
        if (!fileOwnerId.equals(userId)) {
            logger.warn("IDOR ATTEMPT BLOCKED - User {} attempted {} on file {} owned by {}",
                userId, operation, file.getId(), fileOwnerId);
            throw new UnauthorizedException(
                "Unauthorized - You don't have permission to " + operation.toLowerCase() + " this file",
                userId,
                file.getId()
            );
        }
        
        logger.debug("Ownership verified for user: {} - Operation: {} - File: {}", userId, operation, file.getId());
    }
    
    /**
     * Check if file is deleted (soft delete support)
     * @param file The file being accessed
     * @throws UnauthorizedException if file is deleted
     */
    public void checkNotDeleted(FileMetadata file) throws UnauthorizedException {
        if (file.getIsDeleted() != null && file.getIsDeleted()) {
            logger.warn("Attempt to access deleted file: {}", file.getId());
            throw new UnauthorizedException("File has been deleted", null, file.getId());
        }
    }
    
    /**
     * Check if file has been scanned for malware (hook for antivirus integration)
     * @param file The file being accessed
     * @throws UnauthorizedException if file scan status is UNSAFE
     */
    public void checkScanStatus(FileMetadata file) throws UnauthorizedException {
        // (hap ni kwaa ajiri ya antvirus ku scan uploaded files)
        // This is a hook for future antivirus integration
        // Scan status columns will be added to the database schema
        // For now, skip this check - will be implemented when scan_status column is available
        
        // TODO: Uncomment when scan_status column is added to file_metadata table
        // String scanStatus = file.getScanStatus();
        // if (scanStatus != null && "UNSAFE".equalsIgnoreCase(scanStatus)) {
        //     logger.warn("Attempt to download unsafe file: {} - Scan details: {}",
        //         file.getId(), file.getScanDetails());
        //     throw new UnauthorizedException(
        //         "File contains malware or unsafe content - Download blocked",
        //         null,
        //         file.getId()
        //     );
        // }
    }
    
    /**
     * Comprehensive authorization check combining ownership, deletion, and scan status
     * @param file The file being accessed
     * @param userId The user attempting access
     * @param operation The operation being performed
     * @throws UnauthorizedException if any check fails
     */
    public void checkAll(FileMetadata file, String userId, String operation) throws UnauthorizedException {
        checkOwnership(file, userId, operation);
        checkNotDeleted(file);
        checkScanStatus(file);
    }
}
