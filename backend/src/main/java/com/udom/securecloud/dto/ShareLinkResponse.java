package com.udom.securecloud.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShareLinkResponse {
    private Long id;
    private String token;
    private String publicUrl;
    private Long fileId;
    private String fileName;
    private LocalDateTime expiresAt;
    private Integer downloadLimit;
    private Integer downloadCount;
    private boolean passwordProtected;
    private boolean active;
    private LocalDateTime createdAt;
}
