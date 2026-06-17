package com.udom.securecloud.service;

import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.ShareLink;
import com.udom.securecloud.model.SharedFile;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.ShareLinkRepository;
import com.udom.securecloud.repository.SharedFileRepository;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseCleanupService {

    private final FileMetadataRepository fileMetadataRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final SharedFileRepository sharedFileRepository;
    private final MinioClient minioClient;
    private final AuditLogService auditLogService;

    @Value("${minio.bucket.name}")
    private String bucketName;

    // Run every day at 2:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredShares() {
        log.info("Starting cleanup of expired share links and internal shares...");
        LocalDateTime now = LocalDateTime.now();

        // 1. Cleanup expired public ShareLinks
        List<ShareLink> expiredLinks = shareLinkRepository.findByExpiresAtBefore(now);
        if (!expiredLinks.isEmpty()) {
            shareLinkRepository.deleteAll(expiredLinks);
            log.info("Cleaned up {} expired public share links.", expiredLinks.size());
            auditLogService.logAction(null, "SYSTEM", "CLEANUP_SHARE_LINKS", "SYSTEM", null, null, null, "SUCCESS", "Removed " + expiredLinks.size() + " expired links");
        }

        // 2. Cleanup expired internal SharedFiles
        List<SharedFile> expiredShares = sharedFileRepository.findByExpiresAtBefore(now);
        if (!expiredShares.isEmpty()) {
            sharedFileRepository.deleteAll(expiredShares);
            log.info("Cleaned up {} expired internal shares.", expiredShares.size());
            auditLogService.logAction(null, "SYSTEM", "CLEANUP_INTERNAL_SHARES", "SYSTEM", null, null, null, "SUCCESS", "Removed " + expiredShares.size() + " expired internal shares");
        }
    }

    // Run every day at 3:00 AM
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupSoftDeletedFiles() {
        log.info("Starting cleanup of soft-deleted files older than 30 days...");
        // 30-day retention policy
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        List<FileMetadata> filesToPurge = fileMetadataRepository.findByIsDeletedTrueAndUpdatedAtBefore(cutoffDate);
        
        int successCount = 0;
        int failureCount = 0;

        for (FileMetadata metadata : filesToPurge) {
            try {
                // Remove from object storage
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(metadata.getFilePath())
                        .build());

                // Remove from database
                fileMetadataRepository.delete(metadata);
                successCount++;
                
                log.debug("Permanently deleted file: {} (ID: {})", metadata.getOriginalName(), metadata.getId());
            } catch (Exception e) {
                log.error("Failed to delete file from MinIO: {} (ID: {})", metadata.getOriginalName(), metadata.getId(), e);
                failureCount++;
            }
        }

        if (successCount > 0 || failureCount > 0) {
            log.info("Soft-deleted files cleanup complete. Success: {}, Failed: {}", successCount, failureCount);
            auditLogService.logAction(null, "SYSTEM", "CLEANUP_TRASH", "SYSTEM", null, null, null, "SUCCESS", 
                String.format("Permanently deleted %d files, failed %d", successCount, failureCount));
        }
    }
}
