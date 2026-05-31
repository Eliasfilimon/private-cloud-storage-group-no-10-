package com.udom.securecloud.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFolderRequest {
    
    @NotBlank(message = "Folder name is required")
    private String folderName;
    
    private Long parentFolderId; // null = root folder
    
    @Builder.Default
    private String folderColor = "#4A90E2";
}
