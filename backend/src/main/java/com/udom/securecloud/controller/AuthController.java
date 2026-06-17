package com.udom.securecloud.controller;

import com.udom.securecloud.dto.AuthResponse;
import com.udom.securecloud.dto.ChangePasswordRequest;
import com.udom.securecloud.dto.CreateUserRequest;
import com.udom.securecloud.dto.LoginRequest;
import com.udom.securecloud.dto.RefreshTokenRequest;
import com.udom.securecloud.dto.TotpVerifyRequest;
import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.service.AuthService;
import com.udom.securecloud.service.PasswordResetService;
import com.udom.securecloud.service.SessionService;
import com.udom.securecloud.security.RateLimited;
import com.udom.securecloud.security.PasswordValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final SessionService sessionService;
    private final PasswordValidator passwordValidator;

    @PostMapping("/forgot-password")
    @RateLimited("auth")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        passwordResetService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "If the email exists, a password reset link has been sent"));
    }

    @GetMapping("/reset-password/validate")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        boolean valid = passwordResetService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/reset-password")
    @RateLimited("auth")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String token       = request.get("token");
        String newPassword = request.get("newPassword");

        // C2: Validate new password strength before resetting
        if (newPassword == null || !passwordValidator.isValid(newPassword, null)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error",
                "Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character"
            ));
        }

        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
        }
    }

    @PostMapping("/login")
    @RateLimited("auth")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                             HttpServletRequest request) {
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        AuthResponse response = authService.login(loginRequest, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest createUserRequest,
                                                 HttpServletRequest request) {
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        UserResponse response = authService.createUser(createUserRequest, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimited("auth")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody CreateUserRequest createUserRequest,
                                               HttpServletRequest request) {
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        UserResponse response = authService.createUser(createUserRequest, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        UserResponse response = authService.getCurrentUser(username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                               HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.changePassword(username, request, ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/2fa/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> setup2fa(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        Map<String, String> response = authService.setup2fa(username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/enable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> enable2fa(@RequestBody Map<String, String> request,
                                                          HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.enable2fa(username, request.get("code"), ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "2FA enabled successfully"));
    }

    @PostMapping("/2fa/disable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> disable2fa(@RequestBody Map<String, String> request,
                                                           HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.disable2fa(username, request.get("code"), ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "2FA disabled successfully"));
    }
    
    @PostMapping("/refresh")
    @RateLimited("auth")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/activity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> pingActivity(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            sessionService.updateActivity(token);
        }
        return ResponseEntity.ok(Map.of("message", "Activity updated"));
    }
    
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            sessionService.invalidateSession(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // G1 / M7: 2FA second-step verification endpoint
    @PostMapping("/2fa/verify-login")
    @RateLimited("auth")
    public ResponseEntity<AuthResponse> verifyTotpLogin(@Valid @RequestBody TotpVerifyRequest request,
                                                        HttpServletRequest httpRequest) {
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.verifyTotpLogin(
                request.getPendingToken(), request.getCode(), ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
