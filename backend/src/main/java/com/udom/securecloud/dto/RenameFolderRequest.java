package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenameFolderRequest {
    private String newFolderName;
    private String folderColor;
}
