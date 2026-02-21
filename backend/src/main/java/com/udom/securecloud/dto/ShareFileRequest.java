package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareFileRequest {
    private List<Long> userIds;
    private String permission; // VIEW, DOWNLOAD, EDIT
    private Integer expiresInDays; // null means no expiration
}
