package com.udom.securecloud.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ShareFileRequest: DTO for sharing files
 * Validates user IDs, permission, and expiration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareFileRequest {
    
    @NotEmpty(message = "At least one user ID is required")
    private List<@Positive(message = "User ID must be positive") Long> userIds;
    
    @NotNull(message = "Permission is required")
    @Pattern(regexp = "^(VIEW|EDIT|DOWNLOAD)$", 
             message = "Permission must be VIEW, EDIT, or DOWNLOAD")
    private String permission;
    
    @Positive(message = "Expiration days must be positive")
    private Integer expiresInDays;
}
