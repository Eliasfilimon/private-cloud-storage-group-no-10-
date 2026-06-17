package com.udom.securecloud.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class RequestSigningService {

    @Value("${security.request-signing.algorithm:HmacSHA256}")
    private String algorithm;

    @Value("${security.request-signing.enabled:true}")
    private boolean enabled;

    @Value("${jwt.secret:}")
    private String signingKey;

    /**
     * Generate HMAC signature for request
     * @param data Data to sign (typically method + path + timestamp)
     * @return Base64-encoded signature
     */
    public String generateSignature(String data) {
        if (!enabled || signingKey == null || signingKey.isEmpty()) {
            return "";
        }

        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    signingKey.getBytes(StandardCharsets.UTF_8),
                    0,
                    signingKey.getBytes(StandardCharsets.UTF_8).length,
                    algorithm
            );
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Verify HMAC signature
     * @param data Original data
     * @param signature Signature to verify
     * @return true if signature is valid
     */
    public boolean verifySignature(String data, String signature) {
        if (!enabled || signature == null || signature.isEmpty()) {
            return true; // Skip verification if disabled
        }

        try {
            String expectedSignature = generateSignature(data);
            return constantTimeEquals(expectedSignature, signature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }

        return result == 0;
    }

    /**
     * Build signature data from request components
     * @param method HTTP method
     * @param path Request path
     * @param timestamp Request timestamp
     * @return Data to be signed
     */
    public String buildSignatureData(String method, String path, long timestamp) {
        return method + "|" + path + "|" + timestamp;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
