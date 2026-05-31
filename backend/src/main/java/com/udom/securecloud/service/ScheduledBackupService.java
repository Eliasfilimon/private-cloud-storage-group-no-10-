package com.udom.securecloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledBackupService {

    private final BackupService backupService;
    private final AuditLogService auditLogService;

    @Value("${backup.enabled:true}")
    private boolean backupEnabled;

    @Value("${backup.location:./backups}")
    private String backupLocation;

    @Value("${backup.retention-days:30}")
    private int retentionDays;

    @Scheduled(cron = "${backup.schedule:0 0 2 * * *}")
    public void scheduledBackup() {
        if (!backupEnabled) {
            log.info("Scheduled backup is disabled");
            return;
        }

        log.info("Starting scheduled backup at {}", LocalDateTime.now());
        
        try {
            // Create backup directory if it doesn't exist
            Path backupPath = Paths.get(backupLocation);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            // Generate backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "backup_" + timestamp + ".json";
            String zipFileName = "backup_" + timestamp + ".zip";
            Path backupFile = backupPath.resolve(backupFileName);
            Path zipFile = backupPath.resolve(zipFileName);

            // Create backup data
            String adminUsername = "SYSTEM_SCHEDULER";
            var backupData = backupService.createBackup(adminUsername);

            // Write backup to file
            String jsonBackup = convertToJson(backupData);
            Files.writeString(backupFile, jsonBackup);

            // Compress backup
            compressFile(backupFile, zipFile);

            // Delete uncompressed backup
            Files.deleteIfExists(backupFile);

            log.info("Scheduled backup completed successfully: {}", zipFileName);

            // Clean up old backups
            cleanupOldBackups(backupPath);

        } catch (Exception e) {
            log.error("Scheduled backup failed", e);
            auditLogService.logAction(
                null, "SYSTEM_SCHEDULER", "SCHEDULED_BACKUP", "BACKUP", null,
                null, null, "FAILED", "Scheduled backup failed: " + e.getMessage()
            );
        }
    }

    private String convertToJson(Object data) {
        // Simple JSON conversion - in production use Jackson
        return data.toString();
    }

    private void compressFile(Path sourceFile, Path targetFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(targetFile.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            ZipEntry zipEntry = new ZipEntry(sourceFile.getFileName().toString());
            zos.putNextEntry(zipEntry);
            
            byte[] bytes = Files.readAllBytes(sourceFile);
            zos.write(bytes);
            
            zos.closeEntry();
        }
    }

    private void cleanupOldBackups(Path backupPath) throws IOException {
        File backupDir = backupPath.toFile();
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".zip"));

        if (backupFiles != null) {
            long cutoffTime = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);

            for (File file : backupFiles) {
                if (file.lastModified() < cutoffTime) {
                    Files.deleteIfExists(file.toPath());
                    log.info("Deleted old backup: {}", file.getName());
                }
            }
        }
    }
}
