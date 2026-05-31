package com.udom.securecloud.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class TotpService {

    private static final int DIGITS = 6;
    private static final int TIME_STEP = 30;
    private static final String ALGORITHM = "HmacSHA1";
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /**
     * Generate a single, universally compatible QR code URI for all authenticator apps.
     * Uses the standard otpauth:// format (RFC 6238/4226) which is supported by:
     * - Google Authenticator
     * - Microsoft Authenticator
     * - Authy
     * - LastPass Authenticator
     * - 1Password
     * - And any TOTP-compliant app including ngao Authenticator (if compliant)
     */
    public String generateQrCodeUri(String username, String secret, String issuer) {
        // URL encode the label and issuer to handle special characters safely
        String label = URLEncoder.encode(issuer + ":" + username, StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);

        // Build the URI with all standard parameters
        // Using the most compatible format that works across all authenticator apps
        return String.format(
            "otpauth://totp/%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
            label, secret, encodedIssuer, DIGITS, TIME_STEP
        );
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.isEmpty()) {
            return false;
        }
        try {
            long currentTime = Instant.now().getEpochSecond() / TIME_STEP;
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateCode(secret, currentTime + i);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateCode(String secret, long timeStep) throws Exception {
        byte[] key = decodeBase32(secret);
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array();

        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(key, ALGORITHM));
        byte[] hash = mac.doFinal(timeBytes);

        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, DIGITS);
        return String.format("%0" + DIGITS + "d", otp);
    }

    private static String encodeBase32(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int index = 0;
        int digit;
        int currByte;
        int nextByte;

        while (i < data.length) {
            currByte = (data[i] >= 0) ? data[i] : (data[i] + 256);

            if (index > 3) {
                if ((i + 1) < data.length) {
                    nextByte = (data[i + 1] >= 0) ? data[i + 1] : (data[i + 1] + 256);
                } else {
                    nextByte = 0;
                }
                digit = currByte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= nextByte >> (8 - index);
                i++;
            } else {
                digit = (currByte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0) i++;
            }
            sb.append(BASE32_CHARS.charAt(digit));
        }
        return sb.toString();
    }

    private static byte[] decodeBase32(String data) {
        int[] lookup = new int[256];
        for (int i = 0; i < BASE32_CHARS.length(); i++) {
            lookup[BASE32_CHARS.charAt(i)] = i;
            lookup[Character.toLowerCase(BASE32_CHARS.charAt(i))] = i;
        }

        byte[] bytes = new byte[data.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (char c : data.toCharArray()) {
            if (c == '=') break;
            int val = lookup[c];
            buffer <<= 5;
            buffer |= val & 0x1F;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes[count++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return java.util.Arrays.copyOf(bytes, count);
    }
}
