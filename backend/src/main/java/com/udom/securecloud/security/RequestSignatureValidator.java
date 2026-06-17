package com.udom.securecloud.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RequestSignatureValidator {

    @Value("${security.request-signing.enabled:true}")
    private boolean enabled;

    @Value("${security.request-signing.timestamp-tolerance:300000}")
    private long timestampTolerance; // Default: 5 minutes

    private final RequestSigningService signingService;

    public RequestSignatureValidator(RequestSigningService signingService) {
        this.signingService = signingService;
    }

    /**
     * Validate request signature and timestamp
     * @param method HTTP method
     * @param path Request path
     * @param signature Signature from header
     * @param timestamp Timestamp from header
     * @return true if signature and timestamp are valid
     */
    public boolean validateRequest(String method, String path, String signature, String timestamp) {
        if (!enabled) {
            return true; // Skip validation if disabled
        }

        // Validate timestamp
        if (!isTimestampValid(timestamp)) {
            return false;
        }

        // Validate signature
        if (signature == null || signature.isEmpty()) {
            return false;
        }

        String signatureData = signingService.buildSignatureData(method, path, Long.parseLong(timestamp));
        return signingService.verifySignature(signatureData, signature);
    }

    /**
     * Check if timestamp is within acceptable tolerance
     * @param timestamp Timestamp string (milliseconds)
     * @return true if timestamp is valid
     */
    private boolean isTimestampValid(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return false;
        }

        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            long timeDifference = Math.abs(currentTime - requestTime);

            // Check if time difference is within tolerance
            return timeDifference <= timestampTolerance;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Get validation error message
     * @param method HTTP method
     * @param path Request path
     * @param signature Signature from header
     * @param timestamp Timestamp from header
     * @return Error message
     */
    public String getValidationError(String method, String path, String signature, String timestamp) {
        if (!enabled) {
            return null;
        }

        if (timestamp == null || timestamp.isEmpty()) {
            return "Missing X-Request-Timestamp header";
        }

        if (!isTimestampValid(timestamp)) {
            return "Request timestamp is outside acceptable tolerance (5 minutes)";
        }

        if (signature == null || signature.isEmpty()) {
            return "Missing X-Request-Signature header";
        }

        String signatureData = signingService.buildSignatureData(method, path, Long.parseLong(timestamp));
        if (!signingService.verifySignature(signatureData, signature)) {
            return "Invalid request signature";
        }

        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getTimestampTolerance() {
        return timestampTolerance;
    }
}
