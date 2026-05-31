package com.udom.securecloud.service;

import com.udom.securecloud.model.AuditLog;
import com.udom.securecloud.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AuditLogService: Comprehensive audit logging for security events
 * 
 * Logs all file operations:
 * - FILE_UPLOAD: File creation/upload operations
 * - FILE_DOWNLOAD: File retrieval/download operations
 * - FILE_DELETE: File deletion operations
 * - FILE_DECRYPT: File decryption operations (including integrity checks)
 * - ACCESS_FAILED: Failed access attempts (unauthorized, authentication failures)
 * 
 * Includes detailed context:
 * - User information (userId, username)
 * - Operation details (action, resource type, resource id)
 * - Network information (IP address, user agent)
 * - Status (SUCCESS, FAILED)
 * - Detailed messages with reasons for failures
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Full audit logging with all contextual information
     */
    @Transactional
    public void logAction(Long userId, String username, String action, String resourceType,
                         Long resourceId, String ipAddress, String userAgent, 
                         String status, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setStatus(status);
            auditLog.setDetails(details);
            auditLog.setCreatedAt(LocalDateTime.now());

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Ensure audit logging failures don't crash the application
            System.err.println("Failed to log audit action: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Basic user action logging without IP/User Agent
     */
    @Transactional
    public void logAction(Long userId, String username, String action, String details) {
        logAction(userId, username, action, "USER", userId, null, null, "SUCCESS", details);
    }

    /**
     * Log file upload operation
     */
    @Transactional
    public void logFileUpload(Long userId, String username, Long fileId, String fileName, 
                             Long fileSize, Boolean encrypted, String ipAddress, String userAgent) {
        String details = String.format(
            "File uploaded: %s (Size: %d bytes, Encrypted: %s)",
            fileName, fileSize, encrypted ? "Yes" : "No"
        );
        logAction(userId, username, "FILE_UPLOAD", "FILE", fileId, ipAddress, userAgent, "SUCCESS", details);
    }

    /**
     * Log file upload failure
     */
    @Transactional
    public void logFileUploadFailed(Long userId, String username, String fileName, 
                                    String reason, String ipAddress, String userAgent) {
        String details = String.format("Upload failed for %s: %s", fileName, reason);
        logAction(userId, username, "FILE_UPLOAD", "FILE", null, ipAddress, userAgent, "FAILED", details);
    }

    /**
     * Log file download operation
     */
    @Transactional
    public void logFileDownload(Long userId, String username, Long fileId, String fileName, 
                               Long fileSize, String ipAddress, String userAgent) {
        String details = String.format(
            "File downloaded: %s (Size: %d bytes)",
            fileName, fileSize
        );
        logAction(userId, username, "FILE_DOWNLOAD", "FILE", fileId, ipAddress, userAgent, "SUCCESS", details);
    }

    /**
     * Log file download failure
     */
    @Transactional
    public void logFileDownloadFailed(Long userId, String username, Long fileId, String fileName, 
                                     String reason, String ipAddress, String userAgent) {
        String details = String.format("Download failed for %s (ID: %d): %s", fileName, fileId, reason);
        logAction(userId, username, "FILE_DOWNLOAD", "FILE", fileId, ipAddress, userAgent, "FAILED", details);
    }

    /**
     * Log file deletion operation
     */
    @Transactional
    public void logFileDelete(Long userId, String username, Long fileId, String fileName, 
                             Long fileSize, String ipAddress, String userAgent) {
        String details = String.format(
            "File deleted: %s (Size: %d bytes)",
            fileName, fileSize
        );
        logAction(userId, username, "FILE_DELETE", "FILE", fileId, ipAddress, userAgent, "SUCCESS", details);
    }

    /**
     * Log file deletion failure
     */
    @Transactional
    public void logFileDeleteFailed(Long userId, String username, Long fileId, String fileName, 
                                    String reason, String ipAddress, String userAgent) {
        String details = String.format("Delete failed for %s (ID: %d): %s", fileName, fileId, reason);
        logAction(userId, username, "FILE_DELETE", "FILE", fileId, ipAddress, userAgent, "FAILED", details);
    }

    /**
     * Log file decryption operation (explicit tracking of crypto operations)
     */
    @Transactional
    public void logFileDecrypt(Long userId, String username, Long fileId, String fileName, 
                              String ipAddress, String userAgent) {
        String details = String.format("File decrypted for download: %s", fileName);
        logAction(userId, username, "FILE_DECRYPT", "FILE", fileId, ipAddress, userAgent, "SUCCESS", details);
    }

    /**
     * Log failed decryption (integrity check failure = tampering detection)
     */
    @Transactional
    public void logDecryptionFailed(Long userId, String username, Long fileId, String fileName, 
                                    String reason, String ipAddress, String userAgent) {
        String details = String.format(
            "SECURITY ALERT - Decryption failed for %s (ID: %d). Reason: %s. File may be tampered with.",
            fileName, fileId, reason
        );
        logAction(userId, username, "FILE_DECRYPT", "FILE", fileId, ipAddress, userAgent, "FAILED", details);
    }

    /**
     * Log unauthorized access attempts
     */
    @Transactional
    public void logUnauthorizedAccess(Long userId, String username, String action, Long resourceId, 
                                     String resourceName, String ipAddress, String userAgent) {
        String details = String.format(
            "Unauthorized %s attempt for resource: %s (ID: %d)",
            action, resourceName, resourceId
        );
        logAction(userId, username, action, "FILE", resourceId, ipAddress, userAgent, "FAILED", details);
    }

    /**
     * Log authentication/key rotation events
     */
    @Transactional
    public void logKeyManagement(Long userId, String username, String operation, String details, String status) {
        logAction(userId, username, "KEY_MANAGEMENT", "SECURITY", null, null, null, status, 
                 operation + ": " + details);
    }

    /**
     * Log storage quota-related events
     */
    @Transactional
    public void logStorageQuotaExceeded(Long userId, String username, String fileName, 
                                        String ipAddress, String userAgent) {
        String details = String.format("Storage quota exceeded while uploading: %s", fileName);
        logAction(userId, username, "FILE_UPLOAD", "FILE", null, ipAddress, userAgent, "FAILED", details);
    }

    /**
     * Retrieve audit logs with pagination for admin viewing
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(int page, int size, String actionFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (actionFilter != null && !actionFilter.isEmpty()) {
            return auditLogRepository.findByAction(actionFilter, pageable);
        }
        
        return auditLogRepository.findAll(pageable);
    }
}
