package com.udom.securecloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * EncryptionProperties: Configuration for encryption settings
 * 
 * Maps properties from:
 * encryption.master-key=${MASTER_ENCRYPTION_KEY}
 */
@Component
@ConfigurationProperties(prefix = "encryption")
@Data
public class EncryptionProperties {
    /**
     * Master encryption key (256-bit, Base64-encoded)
     * Must be set via MASTER_ENCRYPTION_KEY environment variable
     * Generate with: openssl rand -base64 32
     */
    private String masterKey;
}
