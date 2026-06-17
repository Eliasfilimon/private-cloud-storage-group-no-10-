package com.udom.securecloud.controller;

import com.udom.securecloud.dto.ChangePasswordRequest;
import com.udom.securecloud.dto.UpdateProfileRequest;
import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.model.User;
import com.udom.securecloud.model.UserSettings;
import com.udom.securecloud.repository.UserRepository;
import com.udom.securecloud.repository.UserSettingsRepository;
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
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

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
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        
        UserSettings userSettings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserSettings defaultSettings = UserSettings.builder().user(user).build();
                    return userSettingsRepository.save(defaultSettings);
                });

        Map<String, Object> settings = new HashMap<>();
        settings.put("emailNotifications", userSettings.getEmailNotifications());
        settings.put("storageAlerts", userSettings.getStorageAlerts());
        settings.put("loginAlerts", userSettings.getLoginAlerts());
        settings.put("twoFactorEnabled", user.getTotpEnabled());
        settings.put("sessionTimeout", userSettings.getSessionTimeout());
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody Map<String, Object> settings,
                                                              HttpServletRequest httpRequest) {
        String username = httpRequest.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        UserSettings userSettings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> UserSettings.builder().user(user).build());

        if (settings.containsKey("emailNotifications")) {
            userSettings.setEmailNotifications((Boolean) settings.get("emailNotifications"));
        }
        if (settings.containsKey("storageAlerts")) {
            userSettings.setStorageAlerts((Boolean) settings.get("storageAlerts"));
        }
        if (settings.containsKey("loginAlerts")) {
            userSettings.setLoginAlerts((Boolean) settings.get("loginAlerts"));
        }
        if (settings.containsKey("sessionTimeout")) {
            userSettings.setSessionTimeout((Integer) settings.get("sessionTimeout"));
        }

        userSettingsRepository.save(userSettings);

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
