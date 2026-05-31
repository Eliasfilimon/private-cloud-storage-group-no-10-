package com.udom.securecloud.controller;

import com.udom.securecloud.dto.CreateUserRequest;
import com.udom.securecloud.dto.ExternalStaffDto;
import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.service.AuditLogService;
import com.udom.securecloud.service.AuthService;
import com.udom.securecloud.service.BackupService;
import com.udom.securecloud.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final BackupService backupService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = userService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(page, size, action));
    }

    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Database health check
        try {
            userService.getAllUsers();
            health.put("database", Map.of("status", "UP", "message", "Database connection successful"));
        } catch (Exception e) {
            health.put("database", Map.of("status", "DOWN", "message", e.getMessage()));
        }
        
        // Overall status
        boolean isHealthy = ((Map<?, ?>) health.get("database")).get("status").equals("UP");
        health.put("overallStatus", isHealthy ? "HEALTHY" : "UNHEALTHY");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/activity-timeline")
    public ResponseEntity<Map<String, Object>> getActivityTimeline() {
        Map<String, Object> timeline = userService.getActivityTimeline();
        return ResponseEntity.ok(timeline);
    }

    @PostMapping("/backup/create")
    public ResponseEntity<Map<String, Object>> createBackup(HttpServletRequest request) {
        String adminUsername = request.getUserPrincipal().getName();
        Map<String, Object> backup = backupService.createBackup(adminUsername);
        return ResponseEntity.ok(backup);
    }

    @GetMapping("/backup/status")
    public ResponseEntity<Map<String, Object>> getBackupStatus() {
        Map<String, Object> status = backupService.getBackupStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/backup/history")
    public ResponseEntity<?> getBackupHistory() {
        return ResponseEntity.ok(backupService.getBackupHistory());
    }

    @GetMapping("/backup/{backupId}/download")
    public ResponseEntity<Resource> downloadBackup(@PathVariable Long backupId) {
        try {
            Path path = backupService.getBackupFile(backupId);
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/backup/{backupId}")
    public ResponseEntity<Void> deleteBackup(@PathVariable Long backupId, HttpServletRequest request) {
        String adminUsername = request.getUserPrincipal().getName();
        backupService.deleteBackup(backupId, adminUsername);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long userId, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        userService.toggleUserStatus(userId, username, ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        userService.deleteUser(userId, username, ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestParam("role") String role,
            HttpServletRequest request) {
        String adminUsername = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        UserResponse user = userService.updateUserRole(userId, role, adminUsername, ipAddress, userAgent);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}/storage")
    public ResponseEntity<UserResponse> updateUserStorage(
            @PathVariable Long userId,
            @RequestParam("quotaGb") int quotaGb,
            HttpServletRequest request) {
        String adminUsername = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        UserResponse user = userService.updateUserStorage(userId, quotaGb, adminUsername, ipAddress, userAgent);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest,
                                                   HttpServletRequest request) {
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        UserResponse response = authService.createUser(createUserRequest, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/bulk-upload")
    public ResponseEntity<Map<String, Object>> bulkUploadUsers(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        Map<String, Object> result = userService.bulkUploadUsers(file, ipAddress, userAgent);
        return ResponseEntity.ok(result);
    }

    /**
     * Fetch staff information from external HR system API.
     * This is a placeholder implementation that returns mock data.
     * Replace with actual API integration when credentials are available.
     */
    @GetMapping("/users/fetch-from-hr")
    public ResponseEntity<Map<String, Object>> fetchStaffFromExternalApi(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String college) {
        
        // TODO: Replace with actual external API integration
        // For now, return mock data to demonstrate the feature
        List<ExternalStaffDto> mockStaff = List.of(
            ExternalStaffDto.builder()
                .staffId("STF001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@udom.ac.tz")
                .phoneNumber("+255712345678")
                .department("Computer Science")
                .college("CoICT")
                .role("STAFF")
                .build(),
            ExternalStaffDto.builder()
                .staffId("STF002")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@udom.ac.tz")
                .phoneNumber("+255712345679")
                .department("Information Technology")
                .college("CoICT")
                .role("STAFF")
                .build(),
            ExternalStaffDto.builder()
                .staffId("STF003")
                .firstName("Robert")
                .lastName("Johnson")
                .email("robert.johnson@udom.ac.tz")
                .phoneNumber("+255712345680")
                .department("Software Engineering")
                .college("CoICT")
                .role("STAFF")
                .build()
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("staff", mockStaff);
        response.put("count", mockStaff.size());
        response.put("source", "HR_SYSTEM_MOCK");
        response.put("note", "This is mock data. Connect to actual HR API when credentials are available.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Register users from external HR system data.
     * Accepts a list of staff DTOs and creates user accounts.
     * Password is generated from last name in uppercase.
     */
    @PostMapping("/users/register-from-hr")
    public ResponseEntity<Map<String, Object>> registerUsersFromExternalApi(
            @RequestBody List<ExternalStaffDto> staffList,
            HttpServletRequest request) {
        
        String adminUsername = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        Map<String, Object> result = userService.registerUsersFromExternalApi(
            staffList, adminUsername, ipAddress, userAgent
        );
        
        return ResponseEntity.ok(result);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
