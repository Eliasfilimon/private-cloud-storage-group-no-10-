package com.udom.securecloud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@Slf4j
public class ChecksumService {

    public String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Checksum calculation failed", e);
        }
    }

    public String calculateSHA256(ByteArrayInputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            
            byte[] hash = digest.digest();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Checksum calculation failed", e);
            throw new RuntimeException("Checksum calculation failed", e);
        }
    }

    public boolean verifyChecksum(byte[] data, String expectedChecksum) {
        String actualChecksum = calculateSHA256(data);
        return actualChecksum.equalsIgnoreCase(expectedChecksum);
    }

    public String calculateMD5(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not available", e);
            throw new RuntimeException("Checksum calculation failed", e);
        }
    }
}
