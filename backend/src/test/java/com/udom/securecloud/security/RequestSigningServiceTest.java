package com.udom.securecloud.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestSigningService Tests")
class RequestSigningServiceTest {

    private RequestSigningService signingService;

    @BeforeEach
    void setUp() {
        signingService = new RequestSigningService();
        ReflectionTestUtils.setField(signingService, "enabled", true);
        ReflectionTestUtils.setField(signingService, "algorithm", "HmacSHA256");
        ReflectionTestUtils.setField(signingService, "signingKey", "test-secret-key-for-signing");
    }

    @Test
    @DisplayName("Should generate valid signature")
    void testGenerateSignature() {
        String data = "POST|/api/files/upload|1717500000000";
        String signature = signingService.generateSignature(data);
        
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertTrue(signature.length() > 0);
    }

    @Test
    @DisplayName("Same data should generate same signature")
    void testSignatureDeterministic() {
        String data = "POST|/api/files/upload|1717500000000";
        String signature1 = signingService.generateSignature(data);
        String signature2 = signingService.generateSignature(data);
        
        assertEquals(signature1, signature2);
    }

    @Test
    @DisplayName("Different data should generate different signature")
    void testDifferentDataDifferentSignature() {
        String data1 = "POST|/api/files/upload|1717500000000";
        String data2 = "POST|/api/files/delete|1717500000000";
        
        String signature1 = signingService.generateSignature(data1);
        String signature2 = signingService.generateSignature(data2);
        
        assertNotEquals(signature1, signature2);
    }

    @Test
    @DisplayName("Should verify valid signature")
    void testVerifyValidSignature() {
        String data = "POST|/api/files/upload|1717500000000";
        String signature = signingService.generateSignature(data);
        
        assertTrue(signingService.verifySignature(data, signature));
    }

    @Test
    @DisplayName("Should reject invalid signature")
    void testRejectInvalidSignature() {
        String data = "POST|/api/files/upload|1717500000000";
        String invalidSignature = "invalid-signature-here";
        
        assertFalse(signingService.verifySignature(data, invalidSignature));
    }

    @Test
    @DisplayName("Should reject tampered data")
    void testRejectTamperedData() {
        String data = "POST|/api/files/upload|1717500000000";
        String signature = signingService.generateSignature(data);
        
        String tamperedData = "POST|/api/files/delete|1717500000000";
        assertFalse(signingService.verifySignature(tamperedData, signature));
    }

    @Test
    @DisplayName("Should build signature data correctly")
    void testBuildSignatureData() {
        String method = "POST";
        String path = "/api/files/upload";
        long timestamp = 1717500000000L;
        
        String signatureData = signingService.buildSignatureData(method, path, timestamp);
        
        assertEquals("POST|/api/files/upload|1717500000000", signatureData);
    }

    @Test
    @DisplayName("Should handle null signature gracefully")
    void testNullSignature() {
        String data = "POST|/api/files/upload|1717500000000";
        assertFalse(signingService.verifySignature(data, null));
    }

    @Test
    @DisplayName("Should handle empty signature gracefully")
    void testEmptySignature() {
        String data = "POST|/api/files/upload|1717500000000";
        assertFalse(signingService.verifySignature(data, ""));
    }

    @Test
    @DisplayName("Should be case-sensitive for signature")
    void testCaseSensitiveSignature() {
        String data = "POST|/api/files/upload|1717500000000";
        String signature = signingService.generateSignature(data);
        
        String modifiedSignature = signature.substring(0, signature.length() - 1) + 
                                   (signature.charAt(signature.length() - 1) == 'A' ? 'B' : 'A');
        
        assertFalse(signingService.verifySignature(data, modifiedSignature));
    }

    @Test
    @DisplayName("Should handle special characters in data")
    void testSpecialCharactersInData() {
        String data = "POST|/api/files/upload?file=test&size=100|1717500000000";
        String signature = signingService.generateSignature(data);
        
        assertTrue(signingService.verifySignature(data, signature));
    }

    @Test
    @DisplayName("Should handle unicode characters in data")
    void testUnicodeCharactersInData() {
        String data = "POST|/api/files/upload|文件名|1717500000000";
        String signature = signingService.generateSignature(data);
        
        assertTrue(signingService.verifySignature(data, signature));
    }
}
