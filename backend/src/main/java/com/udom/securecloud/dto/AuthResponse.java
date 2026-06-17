package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String department;
    private Long storageQuota;
    private Long storageUsed;
    private Boolean mustChangePassword;

    // --- 2FA pending flow ---
    /** True when 2FA is required — token/refreshToken will be null until TOTP verified. */
    @Builder.Default
    private Boolean pendingTotp = false;
    /** Short-lived opaque token passed back to /auth/2fa/verify-login */
    private String pendingToken;
}
