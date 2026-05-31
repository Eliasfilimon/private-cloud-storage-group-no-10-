package com.udom.securecloud.controller;

import com.udom.securecloud.dto.LoginRequest;
import com.udom.securecloud.dto.AuthResponse;
import com.udom.securecloud.service.AuthService;
import com.udom.securecloud.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordResetService passwordResetService;

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test@udom.ac.tz");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .token("test-token")
                .username("test@udom.ac.tz")
                .fullName("Test User")
                .role("STAFF")
                .build();

        when(authService.login(any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    public void testForgotPassword() throws Exception {
        when(passwordResetService.requestPasswordReset(any())).thenReturn(true);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"test@udom.ac.tz\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}
