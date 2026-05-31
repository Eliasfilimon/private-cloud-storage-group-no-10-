package com.udom.securecloud.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileVersionResponse {
    private Long id;
    private Long fileId;
    private Integer versionNumber;
    private Long fileSize;
    private String createdByUsername;
    private LocalDateTime createdAt;
}
