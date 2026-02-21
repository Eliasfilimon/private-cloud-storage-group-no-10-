package com.udom.securecloud.service;

import com.udom.securecloud.dto.*;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.udom.securecloud.security.JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        // Update last login time
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Log the action
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

        return AuthResponse.builder()
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
                .build();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, String ipAddress, String userAgent) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(User.Role.valueOf(request.getRole()));
        user.setDepartment(request.getDepartment());
        
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
                .build();
    }

    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check email uniqueness if changed
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists");
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user = userRepository.save(user);

        auditLogService.logAction(user.getId(), user.getUsername(), "PROFILE_UPDATE", "User updated profile");

        return mapToUserResponse(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
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

        // Validate password strength
        if (!isPasswordStrong(request.getNewPassword())) {
            throw new RuntimeException("Password must contain uppercase, lowercase, number and special character");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.logAction(user.getId(), user.getUsername(), "PASSWORD_CHANGE", "User changed password");
    }

    private boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
