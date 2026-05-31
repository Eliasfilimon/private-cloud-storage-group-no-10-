package com.udom.securecloud.security.secrets;

/**
 * SecretsProvider: Interface for pluggable secret management.
 * 
 * Enables dependency injection of different secret sources:
 * - Environment variables (development)
 * - AWS Secrets Manager (production)
 * - Azure Key Vault (production)
 * - HashiCorp Vault (production)
 */
public interface SecretsProvider {
    
    /**
     * Get a secret by name
     * @param secretName The name of the secret
     * @return The secret value
     * @throws SecretsException if secret not found or access denied
     */
    String getSecret(String secretName) throws SecretsException;
    
    /**
     * Get a secret with a default value
     * @param secretName The name of the secret
     * @param defaultValue Default value if not found
     * @return The secret value or defaultValue if not found
     */
    String getSecretOrElse(String secretName, String defaultValue);
    
    /**
     * Check if a secret exists
     * @param secretName The name of the secret
     * @return true if secret exists
     */
    boolean secretExists(String secretName);
    
    /**
     * List all available secret names
     * @return Array of secret names
     */
    String[] listSecretNames();
    
    /**
     * Check if this secrets provider is healthy and accessible
     * @return true if provider is accessible
     */
    boolean isHealthy();
}
