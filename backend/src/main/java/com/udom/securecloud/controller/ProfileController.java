package com.udom.securecloud.controller;

import com.udom.securecloud.dto.ChangePasswordRequest;
import com.udom.securecloud.dto.UpdateProfileRequest;
import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final AuthService authService;

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                      HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        UserResponse response = authService.updateProfile(username, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        authService.changePassword(username, request, ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings(HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        Map<String, Object> settings = new HashMap<>();
        settings.put("emailNotifications", true);
        settings.put("storageAlerts", true);
        settings.put("loginAlerts", true);
        settings.put("twoFactorEnabled", false);
        settings.put("sessionTimeout", 15);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody Map<String, Object> settings,
                                                              HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        // TODO: Save settings to database
        Map<String, String> response = new HashMap<>();
        response.put("message", "Settings updated successfully");
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unused")
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
