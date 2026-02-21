package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedFileResponse {
    private Long id;
    private Long fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long ownerId;
    private String ownerUsername;
    private String ownerFullName;
    private Long sharedWithId;
    private String sharedWithUsername;
    private String sharedWithFullName;
    private String permission;
    private Boolean isActive;
    private LocalDateTime sharedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastAccessedAt;
}
