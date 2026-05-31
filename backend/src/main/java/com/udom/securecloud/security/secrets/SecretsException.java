package com.udom.securecloud.security.secrets;

/**
 * SecretsException: Custom exception for secrets management errors
 */
public class SecretsException extends Exception {
    
    public SecretsException(String message) {
        super(message);
    }
    
    public SecretsException(String message, Throwable cause) {
        super(message, cause);
    }
}
