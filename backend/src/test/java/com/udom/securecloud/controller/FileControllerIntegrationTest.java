package com.udom.securecloud.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("FileController Integration Tests")
class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Setup test data if needed
    }

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without token")
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/files"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate file name in search query")
    void testSearchWithInvalidFileName() throws Exception {
        mockMvc.perform(get("/api/files/search")
                .param("q", "")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should enforce search query length limit")
    void testSearchWithExcessiveQueryLength() throws Exception {
        String longQuery = "a".repeat(101);
        mockMvc.perform(get("/api/files/search")
                .param("q", longQuery)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return proper error response format")
    void testErrorResponseFormat() throws Exception {
        mockMvc.perform(get("/api/files/search")
                .param("q", "")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should include security headers in response")
    void testSecurityHeadersPresent() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().exists("X-API-Version"));
    }

    @Test
    @DisplayName("Should include HSTS header with correct max-age")
    void testHSTSHeaderCorrectValue() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().string("Strict-Transport-Security", 
                        containsString("max-age=31536000")))
                .andExpect(header().string("Strict-Transport-Security", 
                        containsString("includeSubDomains")));
    }

    @Test
    @DisplayName("Should include CSP header")
    void testCSPHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    @DisplayName("Should include Permissions-Policy header")
    void testPermissionsPolicyHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().exists("Permissions-Policy"));
    }

    @Test
    @DisplayName("Should include API version in response header")
    void testAPIVersionHeader() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().stringValues("X-API-Version", "v1"));
    }

    @Test
    @DisplayName("Should handle path traversal attempts in file operations")
    void testPathTraversalPrevention() throws Exception {
        mockMvc.perform(get("/api/files/../../etc/passwd")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate file size parameter")
    void testFileSizeValidation() throws Exception {
        mockMvc.perform(post("/api/files/upload")
                .param("size", "-1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should enforce rate limiting on search endpoint")
    void testRateLimitingOnSearch() throws Exception {
        // Make multiple requests to trigger rate limit
        for (int i = 0; i < 51; i++) {
            mockMvc.perform(get("/api/files/search")
                    .param("q", "test")
                    .header("Authorization", "Bearer valid-token"));
        }
        
        // Next request should be rate limited
        mockMvc.perform(get("/api/files/search")
                .param("q", "test")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Should include Retry-After header in rate limit response")
    void testRetryAfterHeaderInRateLimitResponse() throws Exception {
        // Trigger rate limit
        for (int i = 0; i < 51; i++) {
            mockMvc.perform(get("/api/files/search")
                    .param("q", "test")
                    .header("Authorization", "Bearer valid-token"));
        }
        
        mockMvc.perform(get("/api/files/search")
                .param("q", "test")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    @DisplayName("Should validate request content type")
    void testContentTypeValidation() throws Exception {
        mockMvc.perform(post("/api/files/upload")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
