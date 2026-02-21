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
public class FileResponse {
    private Long id;
    private String fileName;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    private Boolean encrypted;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerUsername;
}
