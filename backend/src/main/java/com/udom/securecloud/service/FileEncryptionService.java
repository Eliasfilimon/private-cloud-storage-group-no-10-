package com.udom.securecloud.service;

import com.udom.securecloud.security.secrets.SecretsProvider;
import com.udom.securecloud.security.secrets.SecretsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * FileEncryptionService: Handles AES-256-GCM encryption with master key wrapping.
 * 
 * Key Features:
 * - AES-256-GCM: Authenticated encryption (confidentiality + integrity)
 * - Master Key Wrapping: File keys are wrapped with a master key before storage
 * - Key Versioning: Enables future key rotation without re-encrypting files
 * - GCM Authentication Tag: Replaces SHA-256 checksum for tamper detection
 */
@Service
public class FileEncryptionService {

    // GCM Mode Configuration
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128; // 128 bits = 16 bytes
    private static final int IV_LENGTH = 12; // 96 bits for GCM (optimal)
    private static final int CURRENT_KEY_VERSION = 1;

    @Autowired
    private SecretsProvider secretsProvider;

    private SecretKey masterKey;

    /**
     * Initialize master key using SecretsProvider abstraction
     */
    public SecretKey getMasterKey() throws Exception {
        if (masterKey != null) {
            return masterKey;
        }

        try {
            String keyString = secretsProvider.getSecret("MASTER_ENCRYPTION_KEY");
            
            // Decode base64-encoded master key
            byte[] decodedKey = Base64.getDecoder().decode(keyString);
            if (decodedKey.length != 32) { // 256 bits = 32 bytes
                throw new RuntimeException("Master key must be 256 bits (32 bytes)");
            }

            masterKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_ALGORITHM);
            return masterKey;
        } catch (SecretsException e) {
            throw new RuntimeException(
                "Master encryption key not available. Ensure MASTER_ENCRYPTION_KEY is set in environment or secrets manager.", e);
        }
    }

    /**
     * Generate a new file encryption key (256-bit AES)
     */
    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    /**
     * Encode key to Base64 string (for temporary use)
     */
    public String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Decode Base64 string to SecretKey
     */
    public SecretKey decodeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_ALGORITHM);
    }

    /**
     * Wrap a file key using the master key before database storage.
     * 
     * Format: [Version(1 byte)] [IV(12 bytes)] [Encrypted Key(40 bytes)] [AuthTag(16 bytes)]
     * Total: 69 bytes when base64 encoded: ~92 characters
     */
    public String wrapKey(SecretKey fileKey) throws Exception {
        SecretKey masterKey = getMasterKey();
        byte[] fileKeyBytes = fileKey.getEncoded();

        // Generate random IV for key wrapping
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Setup GCM cipher for key wrapping
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, gcmSpec);

        // Encrypt file key with version as AAD (Additional Authenticated Data)
        byte[] versionBytes = new byte[]{(byte) CURRENT_KEY_VERSION};
        cipher.updateAAD(versionBytes);

        byte[] encryptedKey = cipher.doFinal(fileKeyBytes);

        // Combine: [Version] [IV] [Encrypted Key + Auth Tag]
        ByteBuffer buffer = ByteBuffer.allocate(1 + IV_LENGTH + encryptedKey.length);
        buffer.put((byte) CURRENT_KEY_VERSION);
        buffer.put(iv);
        buffer.put(encryptedKey);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Unwrap a file key from wrapped storage format.
     */
    public SecretKey unwrapKey(String wrappedKey) throws Exception {
        SecretKey masterKey = getMasterKey();
        byte[] wrappedBytes = Base64.getDecoder().decode(wrappedKey);

        ByteBuffer buffer = ByteBuffer.wrap(wrappedBytes);

        // Extract version
        byte version = buffer.get();
        if (version != CURRENT_KEY_VERSION) {
            throw new RuntimeException("Unsupported key version: " + version);
        }

        // Extract IV
        byte[] iv = new byte[IV_LENGTH];
        buffer.get(iv);

        // Extract encrypted key + auth tag
        byte[] encryptedKey = new byte[buffer.remaining()];
        buffer.get(encryptedKey);

        // Setup GCM cipher for key unwrapping
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec);

        // Verify version as AAD
        byte[] versionBytes = new byte[]{(byte) CURRENT_KEY_VERSION};
        cipher.updateAAD(versionBytes);

        byte[] decryptedKey = cipher.doFinal(encryptedKey);
        return new SecretKeySpec(decryptedKey, 0, decryptedKey.length, KEY_ALGORITHM);
    }

    /**
     * Encrypt file data using AES-256-GCM.
     * 
     * Format: [IV(12 bytes)] [Encrypted Data] [Auth Tag(16 bytes)]
     * The auth tag is automatically appended by GCM mode.
     */
    public byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encryptedData = cipher.doFinal(data);

        // Prepend IV to encrypted data (which includes auth tag)
        ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + encryptedData.length);
        buffer.put(iv);
        buffer.put(encryptedData);

        return buffer.array();
    }

    /**
     * Decrypt file data using AES-256-GCM.
     * 
     * Automatically verifies authentication tag for tampering detection.
     * Throws exception if authentication fails (data was tampered with).
     */
    public byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(encryptedData);

        // Extract IV
        byte[] iv = new byte[IV_LENGTH];
        buffer.get(iv);

        // Remaining data is encrypted data + auth tag
        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        // Setup GCM cipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // This will automatically verify the authentication tag
        try {
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Authentication tag verification failed - data may be tampered with", e);
        }
    }

    /**
     * Get the current key version for versioning support
     */
    public int getCurrentKeyVersion() {
        return CURRENT_KEY_VERSION;
    }
}
