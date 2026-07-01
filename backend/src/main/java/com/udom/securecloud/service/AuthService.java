package com.udom.securecloud.service;

import com.udom.securecloud.dto.AuthResponse;
import com.udom.securecloud.dto.ChangePasswordRequest;
import com.udom.securecloud.dto.CreateUserRequest;
import com.udom.securecloud.dto.LoginRequest;
import com.udom.securecloud.dto.UpdateProfileRequest;
import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.UserRepository;
import com.udom.securecloud.security.BruteForceProtectionService;
import com.udom.securecloud.validation.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /** In-memory store for pending 2FA tokens (username → pendingToken). Cleared after use. */
    private final Map<String, String> pendingTotpTokens = new ConcurrentHashMap<>();

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.udom.securecloud.security.JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;
    private final TotpService totpService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final EmailValidator emailValidator;
    private final SessionService sessionService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        logger.debug("Login attempt for user: {} from IP: {}", loginRequest.getUsername(), ipAddress);
        
        String identifier = loginRequest.getUsername();
        
        // Check if account is locked due to brute force attempts
        if (bruteForceProtectionService.isAccountLocked(identifier)) {
            long lockoutTimeRemaining = bruteForceProtectionService.getLockoutTimeRemaining(identifier);
            auditLogService.logAction(
                null, identifier, "USER_LOGIN", "AUTH", null,
                ipAddress, userAgent, "FAILED",
                "Account locked due to too many failed attempts. Lockout remaining: " + lockoutTimeRemaining + "ms"
            );
            throw new RuntimeException("Account locked due to too many failed login attempts. Please try again later.");
        }
        
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            logger.debug("Authentication successful for user: {}", loginRequest.getUsername());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Record successful attempt (clears failed attempts)
            bruteForceProtectionService.recordSuccessfulAttempt(identifier);
        
        // Fetch user to check 2FA status
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        logger.debug("User last login updated: {}", user.getUsername());

        // ── 2FA Gate ─────────────────────────────────────────────────────────
        if (Boolean.TRUE.equals(user.getTotpEnabled())) {
            // Issue a short-lived, opaque pending token instead of a real JWT
            String pendingToken = generatePendingToken();
            pendingTotpTokens.put(pendingToken, user.getUsername());

            auditLogService.logAction(
                user.getId(), user.getUsername(), "USER_LOGIN_2FA_PENDING", "USER",
                user.getId(), ipAddress, userAgent, "PENDING",
                "Password verified, awaiting TOTP code"
            );

            return AuthResponse.builder()
                    .pendingTotp(true)
                    .pendingToken(pendingToken)
                    .build();
        }
        // ─────────────────────────────────────────────────────────────────────

        String token;
        String refreshToken;
        try {
            token = jwtTokenProvider.generateToken(authentication);
            refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getUsername());
            logger.debug("JWT token and refresh token generated successfully");
        } catch (Exception e) {
            logger.error("Token generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate token: " + e.getMessage());
        }

        // Create server-side session
        try {
            sessionService.createSession(user, token, refreshToken, ipAddress, userAgent);
            logger.debug("Session created for user: {}", user.getUsername());
        } catch (Exception sessionEx) {
            logger.warn("Session creation failed: {}", sessionEx.getMessage());
        }

        // Log the action
        try {
            auditLogService.logAction(
                    user.getId(), user.getUsername(), "USER_LOGIN", "USER",
                    user.getId(), ipAddress, userAgent, "SUCCESS",
                    "User logged in successfully"
            );
        } catch (Exception auditEx) {
            logger.warn("Audit logging failed: {}", auditEx.getMessage());
        }

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .department(user.getDepartment())
                .storageQuota(user.getStorageQuota())
                .storageUsed(user.getStorageUsed())
                .mustChangePassword(user.getMustChangePassword())
                .build();

        logger.debug("Login successful for user: {}", user.getUsername());
        return response;
        
        } catch (Exception e) {
            // Record failed attempt for brute force protection
            bruteForceProtectionService.recordFailedAttempt(identifier);
            
            // Log failed login
            try {
                auditLogService.logAction(
                    null, identifier, "USER_LOGIN", "AUTH", null,
                    ipAddress, userAgent, "FAILED",
                    "Login failed: " + e.getMessage()
                );
            } catch (Exception auditEx) {
                logger.warn("Audit logging failed: {}", auditEx.getMessage());
            }
            
            throw e;
        }
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, String ipAddress, String userAgent) {
        // Validate UDOM email
        EmailValidator.EmailValidationResult emailValidation = emailValidator.validateForRegistration(request.getEmail());
        if (!emailValidation.isValid()) {
            throw new RuntimeException(emailValidation.getMessage());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());

        // Generate temporary password (last name in uppercase)
        String defaultPassword = request.getLastName().toUpperCase();
        user.setPassword(passwordEncoder.encode(defaultPassword));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setFullName(request.getFirstName() + " " + request.getLastName());
        user.setRole(User.Role.valueOf(request.getRole()));
        user.setDepartment(request.getDepartment());
        user.setMustChangePassword(true);

        if ("ADMIN".equals(request.getRole())) {
            user.setStorageQuota(10737418240L);
        } else {
            user.setStorageQuota(5368709120L);
        }
        user.setStorageUsed(0L);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User adminUser = userRepository.findByUsername(currentUsername).orElse(null);

        auditLogService.logAction(
                adminUser != null ? adminUser.getId() : null,
                currentUsername, "USER_CREATE", "USER", savedUser.getId(),
                ipAddress, userAgent, "SUCCESS",
                "New user created: " + savedUser.getUsername()
        );

        // G3: Send welcome email with temporary credentials
        try {
            emailService.sendWelcomeEmail(
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getUsername(),
                defaultPassword
            );
        } catch (Exception ex) {
            logger.warn("Welcome email failed to send for {}: {}", savedUser.getEmail(), ex.getMessage());
        }

        return mapToUserResponse(savedUser);
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .department(user.getDepartment())
                .storageQuota(user.getStorageQuota())
                .storageUsed(user.getStorageUsed())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .totpEnabled(user.getTotpEnabled())
                .build();
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // G7: Invalidate all existing sessions so stolen tokens become worthless
        try {
            sessionService.invalidateAllUserSessions(user);
            logger.info("All sessions invalidated for user {} after password change", username);
        } catch (Exception ex) {
            logger.warn("Session invalidation after password change failed: {}", ex.getMessage());
        }

        auditLogService.logAction(
                user.getId(), user.getUsername(), "PASSWORD_CHANGE", "USER",
                user.getId(), ipAddress, userAgent, "SUCCESS",
                "User changed password — all sessions invalidated"
        );
    }

    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // M2: Update fields individually, then recompute fullName from stored values
        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        // Always recompute fullName from the stored (potentially updated) fields
        user.setFullName(user.getFirstName() + " " + user.getLastName());

        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .storageQuota(user.getStorageQuota())
                .storageUsed(user.getStorageUsed())
                .mustChangePassword(user.getMustChangePassword())
                .build();
    }

    @Transactional
    public Map<String, String> setup2fa(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);

        String qrCodeUri = totpService.generateQrCodeUri(user.getUsername(), secret, "UDOM Cloud Storage");

        Map<String, String> response = new HashMap<>();
        response.put("secret", secret);
        response.put("qrCodeUri", qrCodeUri);
        return response;
    }

    @Transactional
    public void enable2fa(String username, String code, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTotpSecret() == null) {
            throw new RuntimeException("2FA not set up. Please call setup2fa first.");
        }

        if (!totpService.verifyCode(user.getTotpSecret(), code)) {
            throw new RuntimeException("Invalid verification code");
        }

        user.setTotpEnabled(true);
        userRepository.save(user);

        auditLogService.logAction(
                user.getId(),
                user.getUsername(),
                "2FA_ENABLED",
                "USER",
                user.getId(),
                ipAddress,
                userAgent,
                "SUCCESS",
                "User enabled 2FA"
        );
    }

    @Transactional
    public void disable2fa(String username, String code, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getTotpEnabled()) {
            throw new RuntimeException("2FA is not enabled");
        }

        if (user.getTotpSecret() != null && !totpService.verifyCode(user.getTotpSecret(), code)) {
            throw new RuntimeException("Invalid verification code");
        }

        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);

        auditLogService.logAction(
                user.getId(),
                user.getUsername(),
                "2FA_DISABLED",
                "USER",
                user.getId(),
                ipAddress,
                userAgent,
                "SUCCESS",
                "User disabled 2FA"
        );
    }
    
    /**
     * Refresh JWT token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        logger.debug("Refresh token request");
        
        // Validate refresh token
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        
        // Get username from refresh token
        String username = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
        
        // Validate session with refresh token
        var sessionOpt = sessionService.validateRefreshToken(refreshToken);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Session not found or expired");
        }
        
        var session = sessionOpt.get();
        User user = session.getUser();
        
        // Generate new access token
        var authorities = java.util.Collections.singletonList(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        String newAccessToken = jwtTokenProvider.generateToken(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user.getUsername(), null, authorities
            )
        );
        
        // Generate new refresh token (refresh token rotation)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        
        // Invalidate old session and create new one
        sessionService.invalidateSession(session.getToken());
        sessionService.createSession(user, newAccessToken, newRefreshToken, session.getIpAddress(), session.getUserAgent());
        
        logger.info("Token refreshed for user: {}", username);
        
        // Return new refresh token as well (refresh token rotation)
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .department(user.getDepartment())
                .storageQuota(user.getStorageQuota())
                .storageUsed(user.getStorageUsed())
                .mustChangePassword(user.getMustChangePassword())
                .build();
    }

    // ── 2FA pending-token verification ───────────────────────────────────────

    /**
     * Second step of the login flow when 2FA is enabled.
     * Validates the pending token + TOTP code, then issues a real JWT.
     */
    @Transactional
    public AuthResponse verifyTotpLogin(String pendingToken, String code,
                                        String ipAddress, String userAgent) {
        String username = pendingTotpTokens.remove(pendingToken);
        if (username == null) {
            throw new RuntimeException("Invalid or expired pending token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!totpService.verifyCode(user.getTotpSecret(), code)) {
            // Re-insert so the user can retry (with a new code) within the same pending session
            pendingTotpTokens.put(pendingToken, username);
            auditLogService.logAction(
                user.getId(), username, "USER_LOGIN_2FA_FAIL", "USER",
                user.getId(), ipAddress, userAgent, "FAILED", "Invalid TOTP code"
            );
            throw new RuntimeException("Invalid TOTP code");
        }

        var authorities = java.util.Collections.singletonList(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        org.springframework.security.core.userdetails.UserDetails principal =
                org.springframework.security.core.userdetails.User.builder()
                        .username(username)
                        .password("")
                        .authorities(authorities)
                        .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        String token        = jwtTokenProvider.generateToken(auth);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        try {
            sessionService.createSession(user, token, refreshToken, ipAddress, userAgent);
        } catch (Exception ex) {
            logger.warn("Session creation failed after 2FA: {}", ex.getMessage());
        }

        auditLogService.logAction(
            user.getId(), username, "USER_LOGIN", "USER",
            user.getId(), ipAddress, userAgent, "SUCCESS",
            "User logged in successfully (2FA verified)"
        );

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .department(user.getDepartment())
                .storageQuota(user.getStorageQuota())
                .storageUsed(user.getStorageUsed())
                .mustChangePassword(user.getMustChangePassword())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Generates a cryptographically random 16-char temp password. */
    static String generateSecureTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        // Guarantee at least one of each required character class
        sb.append(chars.charAt(rng.nextInt(26)));           // uppercase
        sb.append(chars.charAt(26 + rng.nextInt(26)));     // lowercase
        sb.append(chars.charAt(52 + rng.nextInt(10)));     // digit
        sb.append(chars.charAt(62 + rng.nextInt(5)));      // special
        for (int i = 4; i < 16; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        // Shuffle the guaranteed characters into random positions
        char[] arr = sb.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            char tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return new String(arr);
    }

    /** Generates a short-lived opaque pending token for the 2FA gate. */
    private static String generatePendingToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
