package com.udom.securecloud.controller;

import com.udom.securecloud.model.User;
import com.udom.securecloud.model.UserSettings;
import com.udom.securecloud.repository.UserRepository;
import com.udom.securecloud.repository.UserSettingsRepository;
import com.udom.securecloud.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Principal principal;

    @InjectMocks
    private ProfileController profileController;

    private User user;
    private UserSettings userSettings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("testuser");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setTotpEnabled(true);

        userSettings = new UserSettings();
        userSettings.setUser(user);
        userSettings.setEmailNotifications(true);
        userSettings.setStorageAlerts(false);
        userSettings.setSessionTimeout(30);
    }

    @Test
    void testGetSettings_ExistingSettings() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.of(userSettings));

        ResponseEntity<Map<String, Object>> response = profileController.getSettings(request);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = response.getBody();
        assertEquals(true, body.get("emailNotifications"));
        assertEquals(false, body.get("storageAlerts"));
        assertEquals(true, body.get("twoFactorEnabled"));
        assertEquals(30, body.get("sessionTimeout"));
    }

    @Test
    void testGetSettings_NewSettingsCreated() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Map<String, Object>> response = profileController.getSettings(request);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = response.getBody();
        assertEquals(true, body.get("emailNotifications")); // Default is true
        assertEquals(15, body.get("sessionTimeout")); // Default is 15
        
        verify(userSettingsRepository).save(any(UserSettings.class));
    }

    @Test
    void testUpdateSettings() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.of(userSettings));

        Map<String, Object> updates = new HashMap<>();
        updates.put("sessionTimeout", 60);
        updates.put("emailNotifications", false);

        ResponseEntity<Map<String, String>> response = profileController.updateSettings(updates, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Settings updated successfully", response.getBody().get("message"));

        verify(userSettingsRepository).save(userSettings);
        assertEquals(60, userSettings.getSessionTimeout());
        assertEquals(false, userSettings.getEmailNotifications());
    }
}
