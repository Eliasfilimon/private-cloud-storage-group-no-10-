package com.udom.securecloud.service;

import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleUserStatus(Long userId, String adminUsername, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        User admin = userRepository.findByUsername(adminUsername).orElse(null);

        auditLogService.logAction(
                admin != null ? admin.getId() : null,
                adminUsername,
                user.getIsActive() ? "USER_ACTIVATE" : "USER_DEACTIVATE",
                "USER",
                userId,
                ipAddress,
                userAgent,
                "SUCCESS",
                "User " + (user.getIsActive() ? "activated" : "deactivated") + ": " + user.getUsername()
        );
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId, String adminUsername, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User admin = userRepository.findByUsername(adminUsername).orElse(null);

        String deletedUsername = user.getUsername();
        userRepository.delete(user);

        auditLogService.logAction(
                admin != null ? admin.getId() : null,
                adminUsername,
                "USER_DELETE",
                "USER",
                userId,
                ipAddress,
                userAgent,
                "SUCCESS",
                "User deleted: " + deletedUsername
        );
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
}
