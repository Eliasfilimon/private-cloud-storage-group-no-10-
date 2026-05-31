package com.udom.securecloud.security.secrets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * EnvironmentSecretsProvider: Retrieves secrets from environment variables.
 * 
 * Primary implementation for development and production when environment variables are properly configured.
 * Supports:
 * - MASTER_ENCRYPTION_KEY: Master encryption key for file encryption
 * - JWT_SIGNING_SECRET: JWT token signing secret
 * - DATABASE_PASSWORD: Database password
 * 
 * Security Note: Never logs actual secret values, only logs access attempts
 */
@Service
@Primary
public class EnvironmentSecretsProvider implements SecretsProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentSecretsProvider.class);
    
    private static final String[] SUPPORTED_SECRETS = {
        "MASTER_ENCRYPTION_KEY",
        "JWT_SIGNING_SECRET",
        "DATABASE_PASSWORD"
    };
    
    @Override
    public String getSecret(String secretName) throws SecretsException {
        logger.debug("Retrieving secret: {}", secretName);
        
        String secret = System.getenv(secretName);
        if (secret == null || secret.isEmpty()) {
            logger.warn("Secret not found in environment: {}", secretName);
            throw new SecretsException("Secret not found: " + secretName);
        }
        
        logger.debug("Successfully retrieved secret: {}", secretName);
        return secret;
    }
    
    @Override
    public String getSecretOrElse(String secretName, String defaultValue) {
        logger.debug("Retrieving secret with fallback: {}", secretName);
        
        String secret = System.getenv(secretName);
        if (secret == null || secret.isEmpty()) {
            logger.debug("Secret not found, using default for: {}", secretName);
            return defaultValue;
        }
        
        logger.debug("Successfully retrieved secret: {}", secretName);
        return secret;
    }
    
    @Override
    public boolean secretExists(String secretName) {
        String secret = System.getenv(secretName);
        return secret != null && !secret.isEmpty();
    }
    
    @Override
    public String[] listSecretNames() {
        return SUPPORTED_SECRETS;
    }
    
    @Override
    public boolean isHealthy() {
        // Check if at least one critical secret is available
        boolean hasMasterKey = secretExists("MASTER_ENCRYPTION_KEY");
        boolean hasJwtSecret = secretExists("JWT_SIGNING_SECRET");
        
        if (hasMasterKey && hasJwtSecret) {
            logger.debug("EnvironmentSecretsProvider is healthy");
            return true;
        }
        
        logger.warn("EnvironmentSecretsProvider is not healthy - missing critical secrets");
        return false;
    }
}
