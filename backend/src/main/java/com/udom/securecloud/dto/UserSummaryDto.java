package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal user projection used for file-sharing UI.
 * Exposes only what is needed: id, name, email.
 * Prevents leaking storageQuota, lastLogin, totpEnabled, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private Long   id;
    private String fullName;
    private String email;
    private String department;
}
