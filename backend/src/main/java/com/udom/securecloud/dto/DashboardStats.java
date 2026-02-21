package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Long totalFiles;
    private Long storageUsed;
    private Long storageQuota;
    private Double storagePercentage;
    private Long totalUsers;
    private Long activeUsers;
    private Long recentUploads;
    private Long recentDownloads;
}
