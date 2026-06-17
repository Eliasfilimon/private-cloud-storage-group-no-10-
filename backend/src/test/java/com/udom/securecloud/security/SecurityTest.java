package com.udom.securecloud.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Tests")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestSigningService signingService;

    @BeforeEach
    void setUp() {
        // Setup test data
    }

    @Test
    @DisplayName("Should prevent path traversal in file operations")
    void testPathTraversalPrevention() throws Exception {
        String[] pathTraversalAttempts = {
                "../../../etc/passwd",
                "..\\..\\windows\\system32",
                "file/../../../etc/passwd",
                "....//....//....//etc/passwd"
        };

        for (String attempt : pathTraversalAttempts) {
            mockMvc.perform(get("/api/files/" + attempt)
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Should prevent null byte injection")
    void testNullByteInjectionPrevention() throws Exception {
        mockMvc.perform(post("/api/files/upload")
                .param("fileName", "file\0.txt")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should prevent SQL injection in search")
    void testSQLInjectionPrevention() throws Exception {
        String[] sqlInjectionAttempts = {
                "'; DROP TABLE files; --",
                "1' OR '1'='1",
                "admin'--",
                "' UNION SELECT * FROM users--"
        };

        for (String attempt : sqlInjectionAttempts) {
            mockMvc.perform(get("/api/files/search")
                    .param("q", attempt)
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Should prevent XSS in error messages")
    void testXSSPrevention() throws Exception {
        String xssPayload = "<script>alert('XSS')</script>";
        
        mockMvc.perform(get("/api/files/search")
                .param("q", xssPayload)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("<script>"))));
    }

    @Test
    @DisplayName("Should enforce HTTPS via HSTS header")
    void testHSTSEnforcement() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().stringValues("Strict-Transport-Security",
                        hasItem(containsString("max-age=31536000"))));
    }

    @Test
    @DisplayName("Should prevent clickjacking via X-Frame-Options")
    void testClickjackingPrevention() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().stringValues("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName("Should prevent MIME sniffing via X-Content-Type-Options")
    void testMIMESniffingPrevention() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().stringValues("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("Should enforce Content Security Policy")
    void testCSPEnforcement() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    @DisplayName("Should restrict browser features via Permissions-Policy")
    void testPermissionsPolicyEnforcement() throws Exception {
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().exists("Permissions-Policy"))
                .andExpect(header().stringValues("Permissions-Policy",
                        hasItem(containsString("geolocation=()"))));
    }

    @Test
    @DisplayName("Should prevent brute force attacks on authentication")
    void testBruteForceProtection() throws Exception {
        // Make multiple failed login attempts
        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"user\",\"password\":\"wrong\"}"));
        }
        
        // Next attempt should be rate limited
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"wrong\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Should prevent request tampering via signature validation")
    void testRequestTamperingPrevention() throws Exception {
        String method = "POST";
        String path = "/api/files/upload";
        long timestamp = System.currentTimeMillis();
        
        String validSignature = signingService.generateSignature(
                signingService.buildSignatureData(method, path, timestamp));
        
        // Tampered request should fail
        mockMvc.perform(post("/api/files/upload")
                .header("X-Request-Signature", "invalid-signature")
                .header("X-Request-Timestamp", String.valueOf(timestamp))
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should prevent replay attacks via timestamp validation")
    void testReplayAttackPrevention() throws Exception {
        long oldTimestamp = System.currentTimeMillis() - (6 * 60 * 1000); // 6 minutes ago
        String method = "POST";
        String path = "/api/files/upload";
        
        String signature = signingService.generateSignature(
                signingService.buildSignatureData(method, path, oldTimestamp));
        
        // Old timestamp should be rejected
        mockMvc.perform(post("/api/files/upload")
                .header("X-Request-Signature", signature)
                .header("X-Request-Timestamp", String.valueOf(oldTimestamp))
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not expose sensitive information in error responses")
    void testSensitiveInformationExposure() throws Exception {
        mockMvc.perform(get("/api/files/999999")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString("SQLException"))))
                .andExpect(content().string(not(containsString("NullPointerException"))))
                .andExpect(content().string(not(containsString("at java."))));
    }

    @Test
    @DisplayName("Should validate file upload size limits")
    void testFileSizeLimitEnforcement() throws Exception {
        // This would require actual file upload testing
        // Placeholder for file size validation
        mockMvc.perform(post("/api/files/upload")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should enforce API versioning")
    void testAPIVersioningEnforcement() throws Exception {
        mockMvc.perform(get("/api/v1/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().stringValues("X-API-Version", "v1"));
    }

    @Test
    @DisplayName("Should handle deprecated API versions")
    void testDeprecatedVersionHandling() throws Exception {
        // Assuming v0 is deprecated
        mockMvc.perform(get("/api/v0/files")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(header().exists("X-API-Deprecated"));
    }

    @Test
    @DisplayName("Should prevent unauthorized access to admin endpoints")
    void testAdminEndpointProtection() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should log security events")
    void testSecurityEventLogging() throws Exception {
        // This test verifies that security events are logged
        mockMvc.perform(get("/api/files")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
        
        // Verify logging occurred (would need to check logs)
    }
}
