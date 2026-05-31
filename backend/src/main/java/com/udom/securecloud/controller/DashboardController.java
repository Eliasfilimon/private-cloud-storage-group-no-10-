package com.udom.securecloud.controller;

import com.udom.securecloud.dto.DashboardStats;
import com.udom.securecloud.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getUserStats(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        DashboardStats stats = dashboardService.getUserDashboardStats(username);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStats> getAdminStats() {
        DashboardStats stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
