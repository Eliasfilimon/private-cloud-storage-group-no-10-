package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * C4: Sanitized User projection for backup exports.
 * Deliberately excludes: password hash, totpSecret, wrappedEncryptionKey.
 * An attacker with backup access cannot generate valid 2FA codes or tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupUserDto {
    private Long          id;
    private String        username;
    private String        email;
    private String        firstName;
    private String        lastName;
    private String        fullName;
    private String        department;
    private String        role;
    private Boolean       isActive;
    private Long          storageUsed;
    private Long          storageQuota;
    private Boolean       mustChangePassword;
    private Boolean       totpEnabled;   // boolean flag is fine — secret is excluded
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
