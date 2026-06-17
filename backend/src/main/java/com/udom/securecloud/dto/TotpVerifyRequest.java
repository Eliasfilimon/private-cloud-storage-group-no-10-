package com.udom.securecloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the second step of 2FA login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotpVerifyRequest {

    @NotBlank(message = "Pending token is required")
    private String pendingToken;

    @NotBlank(message = "TOTP code is required")
    @Size(min = 6, max = 6, message = "TOTP code must be exactly 6 digits")
    private String code;
}
