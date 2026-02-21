package com.udom.securecloud.service;

import com.udom.securecloud.dto.DashboardStats;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.AuditLogRepository;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public DashboardStats getUserDashboardStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long totalFiles = fileMetadataRepository.countByUserIdAndIsDeletedFalse(user.getId());
        
        // Get recent uploads (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Long recentUploads = auditLogRepository.countByUserIdAndActionAndCreatedAtAfter(
                user.getId(), "FILE_UPLOAD", sevenDaysAgo);
        
        Long recentDownloads = auditLogRepository.countByUserIdAndActionAndCreatedAtAfter(
                user.getId(), "FILE_DOWNLOAD", sevenDaysAgo);

        double percentage = user.getStorageQuota() > 0 
                ? (user.getStorageUsed() * 100.0) / user.getStorageQuota() 
                : 0.0;

        return DashboardStats.builder()
                .totalFiles(totalFiles)
                .storageUsed(user.getStorageUsed())
                .storageQuota(user.getStorageQuota())
                .storagePercentage(Math.round(percentage * 100.0) / 100.0)
                .recentUploads(recentUploads)
                .recentDownloads(recentDownloads)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardStats getAdminDashboardStats() {
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByIsActiveTrue();
        
        Long totalFiles = fileMetadataRepository.countByIsDeletedFalse();
        
        Long totalStorageUsed = userRepository.findAll().stream()
                .mapToLong(User::getStorageUsed)
                .sum();
        
        Long totalStorageQuota = userRepository.findAll().stream()
                .mapToLong(User::getStorageQuota)
                .sum();

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Long recentUploads = auditLogRepository.countByActionAndCreatedAtAfter("FILE_UPLOAD", sevenDaysAgo);
        Long recentDownloads = auditLogRepository.countByActionAndCreatedAtAfter("FILE_DOWNLOAD", sevenDaysAgo);

        double percentage = totalStorageQuota > 0 
                ? (totalStorageUsed * 100.0) / totalStorageQuota 
                : 0.0;

        return DashboardStats.builder()
                .totalFiles(totalFiles)
                .storageUsed(totalStorageUsed)
                .storageQuota(totalStorageQuota)
                .storagePercentage(Math.round(percentage * 100.0) / 100.0)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .recentUploads(recentUploads)
                .recentDownloads(recentDownloads)
                .build();
    }
}
