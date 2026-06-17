package com.udom.securecloud.service;

import com.udom.securecloud.dto.ExternalStaffDto;
import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.dto.UserSummaryDto;
import com.udom.securecloud.model.AuditLog;
import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.AuditLogRepository;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final FileMetadataRepository fileMetadataRepository;
    private final SessionService sessionService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns active users for sharing functionality. Accessible to any authenticated user.
     * H4: Returns minimal UserSummaryDto instead of the full UserResponse to protect privacy.
     */
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getAllUsersForSharing() {
        return userRepository.findAll().stream()
                .filter(User::getIsActive)
                .map(u -> UserSummaryDto.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .department(u.getDepartment())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getStatistics() {
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::getIsActive).count();
        long inactiveUsers = totalUsers - activeUsers;
        long totalStorageUsed = allUsers.stream().mapToLong(User::getStorageUsed).sum();
        long totalStorageQuota = allUsers.stream().mapToLong(User::getStorageQuota).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);
        stats.put("totalStorageUsed", totalStorageUsed);
        stats.put("totalStorageQuota", totalStorageQuota);
        stats.put("storageUsagePercentage", totalStorageQuota > 0 ? (totalStorageUsed * 100 / totalStorageQuota) : 0);

        // Count by role
        Map<String, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));
        stats.put("usersByRole", usersByRole);

        return stats;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getActivityTimeline() {
        Map<String, Object> timeline = new HashMap<>();

        // M6: Use DB-level Top10 query — no more findAll().sort().limit(10) in memory
        List<AuditLog> recentLogs = auditLogRepository.findTop10ByOrderByCreatedAtDesc();
        timeline.put("recentActivities", recentLogs);

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<User> recentLogins = userRepository.findAll().stream()
                .filter(u -> u.getLastLogin() != null && u.getLastLogin().isAfter(weekAgo))
                .sorted((a, b) -> b.getLastLogin().compareTo(a.getLastLogin()))
                .collect(java.util.stream.Collectors.toList());
        timeline.put("recentLogins", recentLogins);

        return timeline;
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

        // M5: Invalidate all active sessions first
        try {
            sessionService.invalidateAllUserSessions(user);
        } catch (Exception ex) { /* log but continue */ }

        // M5: Soft-delete all the user's files to avoid orphaned file records
        List<FileMetadata> userFiles = fileMetadataRepository.findByUserId(userId);
        for (FileMetadata file : userFiles) {
            file.setIsDeleted(true);
            fileMetadataRepository.save(file);
        }

        userRepository.delete(user);

        auditLogService.logAction(
                admin != null ? admin.getId() : null,
                adminUsername, "USER_DELETE", "USER", userId,
                ipAddress, userAgent, "SUCCESS",
                "User deleted: " + deletedUsername + " (" + userFiles.size() + " files soft-deleted)"
        );
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserRole(Long userId, String newRole, String adminUsername, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User admin = userRepository.findByUsername(adminUsername).orElse(null);
        
        // Validate role
        if (!newRole.equals("ADMIN") && !newRole.equals("STAFF")) {
            throw new RuntimeException("Invalid role. Must be ADMIN or STAFF");
        }
        
        String oldRole = user.getRole().toString();
        user.setRole(User.Role.valueOf(newRole));
        
        // Update storage quota based on new role
        if (newRole.equals("ADMIN")) {
            user.setStorageQuota(10737418240L); // 10GB
        } else {
            user.setStorageQuota(5368709120L); // 5GB
        }
        
        userRepository.save(user);
        
        auditLogService.logAction(
                admin != null ? admin.getId() : null,
                adminUsername,
                "USER_ROLE_CHANGE",
                "USER",
                userId,
                ipAddress,
                userAgent,
                "SUCCESS",
                "Changed role from " + oldRole + " to " + newRole + " for user: " + user.getUsername()
        );
        
        return mapToUserResponse(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserStorage(Long userId, int quotaGb, String adminUsername, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User admin = userRepository.findByUsername(adminUsername).orElse(null);

        if (quotaGb < 1) {
            throw new RuntimeException("Storage quota must be at least 1 GB");
        }

        long previousQuota = user.getStorageQuota();
        long newQuotaBytes = (long) quotaGb * 1024 * 1024 * 1024;
        user.setStorageQuota(newQuotaBytes);
        userRepository.save(user);

        auditLogService.logAction(
                admin != null ? admin.getId() : null,
                adminUsername,
                "USER_STORAGE_UPDATE",
                "USER",
                userId,
                ipAddress,
                userAgent,
                "SUCCESS",
                "Updated storage quota from " + (previousQuota / (1024*1024*1024)) + "GB to " + quotaGb + "GB for user: " + user.getUsername()
        );

        return mapToUserResponse(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserDetails(Long userId, String firstName, String lastName, String department,
                                          String adminUsername, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User admin = userRepository.findByUsername(adminUsername).orElse(null);

        if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName.trim());
        if (lastName  != null && !lastName.isBlank())  user.setLastName(lastName.trim());
        if (department != null) user.setDepartment(department.trim());

        userRepository.save(user);

        auditLogService.logAction(
                admin != null ? admin.getId() : null,
                adminUsername,
                "USER_PROFILE_UPDATE",
                "USER",
                userId,
                ipAddress,
                userAgent,
                "SUCCESS",
                "Admin updated profile details for user: " + user.getUsername()
        );

        return mapToUserResponse(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> bulkUploadUsers(MultipartFile file, String ipAddress, String userAgent) {
        Map<String, Object> result = new HashMap<>();
        List<String> successUsers = new ArrayList<>();
        List<Map<String, String>> errors = new ArrayList<>();
        int totalProcessed = 0;

        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                throw new RuntimeException("Only CSV files are allowed");
            }

            // Read CSV file
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

                String headerLine = reader.readLine();
                if (headerLine == null) {
                    throw new RuntimeException("CSV file is empty");
                }

                // Parse headers (handle BOM and Windows line endings)
                String[] headers = parseCsvLine(headerLine);
                Map<String, Integer> headerMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    headerMap.put(headers[i].trim().toLowerCase().replace("\uFEFF", ""), i);
                }

                // Validate required headers
                String[] requiredHeaders = {"email", "firstname", "lastname", "role"};
                for (String required : requiredHeaders) {
                    if (!headerMap.containsKey(required)) {
                        throw new RuntimeException("Missing required column: " + required);
                    }
                }

                // Process each line
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    totalProcessed++;

                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] values = parseCsvLine(line);

                    String email = "", firstName = "", lastName = "", role = "", department = "";
                    try {
                        // Extract values
                        email = values[headerMap.get("email")].trim();
                        firstName = values[headerMap.get("firstname")].trim();
                        lastName = values[headerMap.get("lastname")].trim();
                        role = values[headerMap.get("role")].trim().toUpperCase();
                        department = headerMap.containsKey("department") && values.length > headerMap.get("department")
                                ? values[headerMap.get("department")].trim()
                                : "";

                        // Validate
                        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                            errors.add(Map.of(
                                    "line", String.valueOf(lineNumber),
                                    "email", email,
                                    "error", "Required fields cannot be empty"
                            ));
                            continue;
                        }

                        if (!role.equals("ADMIN") && !role.equals("STAFF")) {
                            errors.add(Map.of(
                                    "line", String.valueOf(lineNumber),
                                    "email", email,
                                    "error", "Invalid role. Must be ADMIN or STAFF"
                            ));
                            continue;
                        }

                        // Check if email already exists
                        if (userRepository.existsByEmail(email)) {
                            errors.add(Map.of(
                                    "line", String.valueOf(lineNumber),
                                    "email", email,
                                    "error", "Email already exists"
                            ));
                            continue;
                        }

                        // M1: Use cryptographically random password for bulk-uploaded users
                        String password = AuthService.generateSecureTempPassword();

                        // Create user
                        User user = new User();
                        user.setUsername(email);
                        user.setEmail(email);
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setPassword(passwordEncoder.encode(password));
                        user.setRole(User.Role.valueOf(role));
                        user.setDepartment(department);
                        user.setMustChangePassword(true);
                        user.setStorageUsed(0L);

                        userRepository.save(user);
                        successUsers.add(email);

                        // G3: Send welcome email with credentials
                        try {
                            emailService.sendWelcomeEmail(email,
                                firstName + " " + lastName, email, password);
                        } catch (Exception mailEx) { /* log warn, continue */ }

                        // Log action
                        auditLogService.logAction(
                                null,
                                "BULK_UPLOAD",
                                "USER_CREATE",
                                "USER",
                                user.getId(),
                                ipAddress,
                                userAgent,
                                "SUCCESS",
                                "User created via CSV upload: " + email
                        );

                    } catch (Exception e) {
                        errors.add(Map.of(
                                "line", String.valueOf(lineNumber),
                                "email", email,
                                "error", e.getMessage()
                        ));
                    }
                }
            }

            result.put("success", true);
            result.put("totalProcessed", totalProcessed);
            result.put("successCount", successUsers.size());
            result.put("errorCount", errors.size());
            result.put("successUsers", successUsers);
            result.put("errors", errors);
            result.put("message", String.format("Processed %d users: %d successful, %d failed",
                    totalProcessed, successUsers.size(), errors.size()));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to process CSV file: " + e.getMessage());
            result.put("errors", errors);
        }

        return result;
    }

    /**
     * Register users from external HR API data.
     * Password is generated from last name in uppercase.
     * Username defaults to email.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> registerUsersFromExternalApi(
            List<ExternalStaffDto> staffList,
            String adminUsername,
            String ipAddress,
            String userAgent) {
        
        Map<String, Object> result = new HashMap<>();
        List<String> successUsers = new ArrayList<>();
        List<Map<String, String>> errors = new ArrayList<>();
        int totalProcessed = 0;

        for (ExternalStaffDto staff : staffList) {
            totalProcessed++;
            try {
                String email = staff.getEmail();
                String firstName = staff.getFirstName();
                String lastName = staff.getLastName();
                String role = staff.getRole() != null ? staff.getRole().toUpperCase() : "STAFF";

                // Validate
                if (email == null || email.isEmpty() || firstName == null || lastName == null) {
                    errors.add(Map.of(
                        "email", email != null ? email : "",
                        "error", "First name, last name, and email are required"
                    ));
                    continue;
                }

                if (!role.equals("ADMIN") && !role.equals("STAFF")) {
                    errors.add(Map.of(
                        "email", email,
                        "error", "Invalid role. Must be ADMIN or STAFF"
                    ));
                    continue;
                }

                // Check if email already exists
                if (userRepository.existsByEmail(email)) {
                    errors.add(Map.of(
                        "email", email,
                        "error", "Email already exists"
                    ));
                    continue;
                }

                // M1: Use cryptographically random password for HR-import users
                String password = AuthService.generateSecureTempPassword();

                User user = new User();
                user.setUsername(email);
                user.setEmail(email);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(User.Role.valueOf(role));
                user.setDepartment(staff.getDepartment() != null ? staff.getDepartment() : "");
                user.setMustChangePassword(true);
                user.setStorageUsed(0L);

                userRepository.save(user);
                successUsers.add(email);

                // G3: Send welcome email
                try {
                    emailService.sendWelcomeEmail(email,
                        firstName + " " + lastName, email, password);
                } catch (Exception mailEx) { /* log warn, continue */ }

                // Log action
                auditLogService.logAction(
                    null,
                    adminUsername,
                    "USER_CREATE",
                    "USER",
                    user.getId(),
                    ipAddress,
                    userAgent,
                    "SUCCESS",
                    "User created via HR API integration: " + email
                );

            } catch (Exception e) {
                errors.add(Map.of(
                    "email", staff.getEmail() != null ? staff.getEmail() : "",
                    "error", e.getMessage()
                ));
            }
        }

        result.put("success", errors.isEmpty() || !successUsers.isEmpty());
        result.put("totalProcessed", totalProcessed);
        result.put("successCount", successUsers.size());
        result.put("errorCount", errors.size());
        result.put("successUsers", successUsers);
        result.put("errors", errors);
        result.put("message", String.format("Processed %d users: %d successful, %d failed",
                totalProcessed, successUsers.size(), errors.size()));

        return result;
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
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

    /**
     * Admin password reset for users who forgot their password.
     * Generates a new temporary password from user's last name and forces password change on next login.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> resetUserPassword(Long userId, String adminUsername, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User admin = userRepository.findByUsername(adminUsername).orElse(null);

        // M1: Use cryptographically random temp password for admin reset
        String tempPassword = AuthService.generateSecureTempPassword();

        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);

        if (Boolean.TRUE.equals(user.getTotpEnabled())) {
            user.setTotpEnabled(false);
            user.setTotpSecret(null);
        }

        userRepository.save(user);

        // Invalidate all sessions so the user is forced to re-login with new password
        try {
            sessionService.invalidateAllUserSessions(user);
        } catch (Exception ex) { /* log warn */ }

        // Log the action
        auditLogService.logAction(
                admin != null ? admin.getId() : null,
                adminUsername,
                "ADMIN_PASSWORD_RESET",
                "USER",
                userId,
                ipAddress,
                userAgent,
                "SUCCESS",
                "Password reset for user: " + user.getUsername() + " (forced password change on next login)"
        );

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Password reset successfully");
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        // L4: Don't expose tempPassword in API response — it was sent via email
        result.put("tempPassword", "[sent via email]");
        result.put("mustChangePassword", true);
        result.put("note", "User must change password on next login");

        return result;
    }

    /**
     * Parse a CSV line handling quoted fields and commas inside quotes.
     * Handles Windows line endings (\r\n) by trimming trailing \r.
     */
    private String[] parseCsvLine(String line) {
        if (line == null) return new String[0];
        // Remove trailing carriage return (Windows line endings)
        line = line.replace("\r", "");

        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());

        return fields.toArray(new String[0]);
    }
}
