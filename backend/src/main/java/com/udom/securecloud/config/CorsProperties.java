package com.udom.securecloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CorsProperties: Configuration for CORS (Cross-Origin Resource Sharing) settings
 * 
 * Maps properties from:
 * cors.allowed-origins=<url>
 */
@Component
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsProperties {
    /**
     * Allowed origins for CORS requests
     * Default: http://localhost:3000
     * Can be comma-separated for multiple origins
     */
    private String allowedOrigins = "http://localhost:3000";
}
