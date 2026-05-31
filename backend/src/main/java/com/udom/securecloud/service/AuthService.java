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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.udom.securecloud.security.JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;
    private final TotpService totpService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final EmailValidator emailValidator;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        System.out.println("=== LOGIN START ===");
        System.out.println("Username: " + loginRequest.getUsername());
        System.out.println("IP: " + ipAddress);
        
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

            System.out.println("Authentication successful");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Record successful attempt (clears failed attempts)
            bruteForceProtectionService.recordSuccessfulAttempt(identifier);
        
        String token;
        try {
            token = jwtTokenProvider.generateToken(authentication);
            System.out.println("Token generated: " + (token != null ? "Yes" : "No"));
        } catch (Exception e) {
            System.out.println("Token generation failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate token: " + e.getMessage());
        }

        // Update last login time
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        System.out.println("User updated: " + user.getUsername());

        // Log the action
        try {
            auditLogService.logAction(
                    user.getId(),
                    user.getUsername(),
                    "USER_LOGIN",
                    "USER",
                    user.getId(),
                    ipAddress,
                    userAgent,
                    "SUCCESS",
                    "User logged in successfully"
            );
        } catch (Exception auditEx) {
            System.out.println("Audit logging failed: " + auditEx.getMessage());
        }

        AuthResponse response = AuthResponse.builder()
                .token(token)
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

        System.out.println("Response built successfully");
        System.out.println("=== LOGIN END ===");
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
                System.out.println("Audit logging failed: " + auditEx.getMessage());
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
        // Set username to email by default
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());

        // Generate password from last name in uppercase
        String lastName = request.getLastName() != null ? request.getLastName().toUpperCase() : "PASSWORD";
        String defaultPassword = lastName.isEmpty() ? "PASSWORD" : lastName;
        user.setPassword(passwordEncoder.encode(defaultPassword));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setFullName(request.getFirstName() + " " + request.getLastName());
        user.setRole(User.Role.valueOf(request.getRole()));
        user.setDepartment(request.getDepartment());
        user.setMustChangePassword(true); // Force password change on first login

        // Set storage quota based on role
        if ("ADMIN".equals(request.getRole())) {
            user.setStorageQuota(10737418240L); // 10GB
        } else {
            user.setStorageQuota(5368709120L); // 5GB
        }

        user.setStorageUsed(0L);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // Get current admin user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User adminUser = userRepository.findByUsername(currentUsername).orElse(null);

        // Log the action
        auditLogService.logAction(
                adminUser != null ? adminUser.getId() : null,
                currentUsername,
                "USER_CREATE",
                "USER",
                savedUser.getId(),
                ipAddress,
                userAgent,
                "SUCCESS",
                "New user created: " + savedUser.getUsername()
        );

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

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // Clear the flag after password change
        userRepository.save(user);

        auditLogService.logAction(
                user.getId(),
                user.getUsername(),
                "PASSWORD_CHANGE",
                "USER",
                user.getId(),
                ipAddress,
                userAgent,
                "SUCCESS",
                "User changed password"
        );
    }

    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user fields
        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
            user.setFullName(request.getFirstName() + " " + request.getLastName());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }

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
}
