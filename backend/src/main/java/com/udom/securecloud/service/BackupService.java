package com.udom.securecloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.udom.securecloud.dto.BackupUserDto;
import com.udom.securecloud.model.BackupRecord;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final BackupRecordRepository backupRecordRepository;
    private final AuditLogService auditLogService;

    @Value("${file.backup.location:./backups}")
    private String backupLocation;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> createBackup(String adminUsername) {
        Map<String, Object> result = new HashMap<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "backup_" + timestamp + ".zip";

        try {
            // Ensure backup directory exists
            Path backupDir = Paths.get(backupLocation);
            Files.createDirectories(backupDir);
            Path backupPath = backupDir.resolve(fileName);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            // Gather data
            var rawUsers  = userRepository.findAll();
            var auditLogs = auditLogRepository.findAll();
            var files     = fileMetadataRepository.findAll();

            // C4: Convert User entities to sanitized DTOs — no passwords or TOTP secrets exported
            List<BackupUserDto> users = rawUsers.stream().map(u -> BackupUserDto.builder()
                    .id(u.getId())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .fullName(u.getFullName())
                    .department(u.getDepartment())
                    .role(u.getRole().name())
                    .isActive(u.getIsActive())
                    .storageUsed(u.getStorageUsed())
                    .storageQuota(u.getStorageQuota())
                    .mustChangePassword(u.getMustChangePassword())
                    .totpEnabled(u.getTotpEnabled())   // flag only — no secret
                    .createdAt(u.getCreatedAt())
                    .lastLogin(u.getLastLogin())
                    .build()
            ).collect(Collectors.toList());

            // Write ZIP
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupPath.toFile()))) {

                // users.json (sanitized — no password hashes or TOTP secrets)
                zos.putNextEntry(new ZipEntry("users.json"));
                zos.write(mapper.writeValueAsBytes(users));
                zos.closeEntry();

                // audit_logs.json
                zos.putNextEntry(new ZipEntry("audit_logs.json"));
                zos.write(mapper.writeValueAsBytes(auditLogs));
                zos.closeEntry();

                // file_metadata.json
                zos.putNextEntry(new ZipEntry("file_metadata.json"));
                zos.write(mapper.writeValueAsBytes(files));
                zos.closeEntry();

                // backup_info.json
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("backupDate", LocalDateTime.now().toString());
                info.put("createdBy", adminUsername);
                info.put("userCount", users.size());
                info.put("auditLogCount", auditLogs.size());
                info.put("fileMetadataCount", files.size());
                zos.putNextEntry(new ZipEntry("backup_info.json"));
                zos.write(mapper.writeValueAsBytes(info));
                zos.closeEntry();
            }

            long fileSize = Files.size(backupPath);

            // Save record
            BackupRecord record = BackupRecord.builder()
                    .fileName(fileName)
                    .filePath(backupPath.toAbsolutePath().toString())
                    .fileSize(fileSize)
                    .backupType("FULL")
                    .status("SUCCESS")
                    .message("Backup created successfully")
                    .userCount(users.size())
                    .fileCount(files.size())
                    .auditLogCount(auditLogs.size())
                    .createdBy(adminUsername)
                    .build();
            backupRecordRepository.save(record);

            // Audit log
            auditLogService.logAction(null, adminUsername, "SYSTEM_BACKUP", "BACKUP", record.getId(),
                    null, null, "SUCCESS",
                    String.format("Backup created: %s (%d users, %d files, %d logs)",
                            fileName, users.size(), files.size(), auditLogs.size()));

            result.put("status", "SUCCESS");
            result.put("message", "Backup created successfully");
            result.put("fileName", fileName);
            result.put("fileSize", fileSize);
            result.put("userCount", users.size());
            result.put("fileCount", files.size());
            result.put("auditLogCount", auditLogs.size());
            result.put("backupDate", LocalDateTime.now());

            log.info("Backup created by {}: {} ({} bytes)", adminUsername, fileName, fileSize);

        } catch (Exception e) {
            log.error("Backup failed", e);

            // Save failed record
            BackupRecord record = BackupRecord.builder()
                    .fileName(fileName)
                    .filePath("")
                    .fileSize(0L)
                    .backupType("FULL")
                    .status("FAILED")
                    .message("Backup failed: " + e.getMessage())
                    .userCount(0)
                    .fileCount(0)
                    .auditLogCount(0)
                    .createdBy(adminUsername)
                    .build();
            backupRecordRepository.save(record);

            auditLogService.logAction(null, adminUsername, "SYSTEM_BACKUP", "BACKUP", null,
                    null, null, "FAILED", "Backup failed: " + e.getMessage());

            result.put("status", "FAILED");
            result.put("message", "Backup failed: " + e.getMessage());
        }

        return result;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getBackupStatus() {
        Map<String, Object> status = new HashMap<>();

        long userCount = userRepository.count();
        long auditLogCount = auditLogRepository.count();
        long storageUsed = userRepository.findAll().stream().mapToLong(User::getStorageUsed).sum();

        status.put("userCount", userCount);
        status.put("auditLogCount", auditLogCount);
        status.put("storageUsed", storageUsed);

        // Last successful backup
        List<BackupRecord> successBackups = backupRecordRepository.findByStatusOrderByCreatedAtDesc("SUCCESS");
        if (!successBackups.isEmpty()) {
            BackupRecord last = successBackups.get(0);
            status.put("lastBackupDate", last.getCreatedAt());
            status.put("lastBackupFileName", last.getFileName());
            status.put("lastBackupSize", last.getFileSize());
        }

        status.put("totalBackups", backupRecordRepository.count());

        return status;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<BackupRecord> getBackupHistory() {
        return backupRecordRepository.findAllByOrderByCreatedAtDesc();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Path getBackupFile(Long backupId) {
        BackupRecord record = backupRecordRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("Backup not found"));

        Path path = Paths.get(record.getFilePath());
        if (!Files.exists(path)) {
            throw new RuntimeException("Backup file not found on disk");
        }
        return path;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBackup(Long backupId, String adminUsername) {
        BackupRecord record = backupRecordRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("Backup not found"));

        // Delete file from disk
        try {
            Path path = Paths.get(record.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete backup file: {}", record.getFilePath(), e);
        }

        backupRecordRepository.delete(record);

        auditLogService.logAction(null, adminUsername, "BACKUP_DELETE", "BACKUP", backupId,
                null, null, "SUCCESS", "Backup deleted: " + record.getFileName());
    }
}
