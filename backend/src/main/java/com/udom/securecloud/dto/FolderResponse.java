package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderResponse {
    private Long id;
    private String folderName;
    private Long parentFolderId;
    private String folderColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FolderResponse> subFolders;
    private List<FileResponse> files;
    private Long totalSize;
    private Integer fileCount;
}
