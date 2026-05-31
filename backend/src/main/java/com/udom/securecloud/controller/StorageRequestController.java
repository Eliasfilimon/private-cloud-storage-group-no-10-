package com.udom.securecloud.controller;

import com.udom.securecloud.dto.ApproveStorageRequest;
import com.udom.securecloud.dto.StorageRequestDto;
import com.udom.securecloud.service.StorageRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/storage-requests")
@RequiredArgsConstructor
public class StorageRequestController {

    private final StorageRequestService storageRequestService;

    @PostMapping
    public ResponseEntity<StorageRequestDto> createRequest(
            @Valid @RequestBody StorageRequestDto requestDto,
            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        StorageRequestDto created = storageRequestService.createRequest(username, requestDto, ipAddress, userAgent);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<StorageRequestDto>> getMyRequests(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        return ResponseEntity.ok(storageRequestService.getUserRequests(username));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StorageRequestDto>> getAllRequests(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(storageRequestService.getAllRequests(status, pageable));
    }

    @GetMapping("/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", storageRequestService.getPendingRequestCount());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StorageRequestDto> approveRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody ApproveStorageRequest approvalDto,
            HttpServletRequest request) {
        String adminUsername = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        StorageRequestDto approved = storageRequestService.approveRequest(
                requestId, approvalDto, adminUsername, ipAddress, userAgent);
        return ResponseEntity.ok(approved);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long requestId,
            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        storageRequestService.cancelRequest(requestId, username);
        return ResponseEntity.ok().build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
