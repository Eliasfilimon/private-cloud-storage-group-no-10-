package com.udom.securecloud.dto;

import com.udom.securecloud.validation.ValidFolderName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FolderCreateRequest: DTO for creating folders
 * Validates folder name and optional parent folder ID
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderCreateRequest {
    
    @NotBlank(message = "Folder name is required")
    @ValidFolderName(message = "Invalid folder name")
    private String folderName;
    
    @Positive(message = "Parent folder ID must be positive")
    private Long parentFolderId;
}
