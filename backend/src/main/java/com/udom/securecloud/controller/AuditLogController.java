package com.udom.securecloud.controller;

import com.udom.securecloud.model.AuditLog;
import com.udom.securecloud.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        String username = request.getUserPrincipal().getName();
        Page<AuditLog> logs = auditLogService.getAuditLogs(page, size, action);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, String>> exportAuditLogs(
            @RequestParam(required = false) String action,
            HttpServletRequest request) {
        
        String username = request.getUserPrincipal().getName();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Audit logs export feature coming soon");
        response.put("note", "CSV export functionality will be implemented in next version");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAdminAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AuditLog> logs = auditLogService.getAuditLogs(page, size, action);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/admin/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> exportAllAuditLogs(
            @RequestParam(required = false) String action) {
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Audit logs export feature coming soon");
        response.put("note", "CSV export functionality will be implemented in next version");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", 0);
        stats.put("successfulActions", 0);
        stats.put("failedActions", 0);
        stats.put("message", "Audit statistics feature coming soon");
        return ResponseEntity.ok(stats);
    }
}
