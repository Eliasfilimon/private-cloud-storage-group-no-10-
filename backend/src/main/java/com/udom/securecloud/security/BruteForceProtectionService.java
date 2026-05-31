package com.udom.securecloud.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class BruteForceProtectionService {

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lockout-duration:900000}")
    private long lockoutDuration; // milliseconds

    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lockedAccounts = new ConcurrentHashMap<>();

    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final String LOCKOUT_PREFIX = "lockout:";

    public void recordFailedAttempt(String identifier) {
        LoginAttempt attempt = loginAttempts.compute(identifier, (key, existing) -> {
            if (existing == null) {
                return new LoginAttempt(1, System.currentTimeMillis() + lockoutDuration);
            }
            return new LoginAttempt(existing.count + 1, System.currentTimeMillis() + lockoutDuration);
        });

        if (attempt.count >= maxAttempts) {
            lockoutAccount(identifier);
        }
    }

    public void recordSuccessfulAttempt(String identifier) {
        loginAttempts.remove(identifier);
        lockedAccounts.remove(identifier);
    }

    public boolean isAccountLocked(String identifier) {
        Long lockoutTime = lockedAccounts.get(identifier);
        if (lockoutTime == null) {
            return false;
        }
        
        // Check if lockout has expired
        if (System.currentTimeMillis() > lockoutTime) {
            lockedAccounts.remove(identifier);
            loginAttempts.remove(identifier);
            return false;
        }
        
        return true;
    }

    public Long getRemainingAttempts(String identifier) {
        if (isAccountLocked(identifier)) {
            return 0L;
        }
        LoginAttempt attempt = loginAttempts.get(identifier);
        if (attempt == null) {
            return (long) maxAttempts;
        }
        return Math.max(0, maxAttempts - (long) attempt.count);
    }

    private void lockoutAccount(String identifier) {
        lockedAccounts.put(identifier, System.currentTimeMillis() + lockoutDuration);
    }

    public Long getLockoutTimeRemaining(String identifier) {
        Long lockoutTime = lockedAccounts.get(identifier);
        if (lockoutTime == null) {
            return 0L;
        }
        long remaining = lockoutTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    private static class LoginAttempt {
        int count;
        long expiryTime;

        LoginAttempt(int count, long expiryTime) {
            this.count = count;
            this.expiryTime = expiryTime;
        }
    }
}
