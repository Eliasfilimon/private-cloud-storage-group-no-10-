package com.udom.securecloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JwtProperties: Configuration for JWT (JSON Web Token) settings
 * 
 * Maps properties from:
 * jwt.secret=${JWT_SIGNING_SECRET}
 * jwt.expiration=<milliseconds>
 * jwt.refresh-secret=${JWT_REFRESH_SECRET}
 * jwt.refresh-expiration=<milliseconds>
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    /**
     * JWT signing secret (Base64-encoded)
     * Generate with: openssl rand -base64 32
     * Must be set via JWT_SIGNING_SECRET environment variable
     */
    private String secret;
    
    /**
     * JWT token expiration time in milliseconds
     * Default: 900000 (15 minutes)
     */
    private long expiration = 900000;
    
    /**
     * JWT refresh token secret (Base64-encoded)
     * Generate with: openssl rand -base64 32
     * Must be set via JWT_REFRESH_SECRET environment variable
     */
    private String refreshSecret;
    
    /**
     * JWT refresh token expiration time in milliseconds
     * Default: 604800000 (7 days)
     */
    private long refreshExpiration = 604800000;
}
