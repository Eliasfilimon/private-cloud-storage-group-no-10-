package com.udom.securecloud.service;

import com.udom.securecloud.dto.ApproveStorageRequest;
import com.udom.securecloud.dto.StorageRequestDto;
import com.udom.securecloud.model.StorageRequest;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.StorageRequestRepository;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorageRequestService {

    private final StorageRequestRepository storageRequestRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public StorageRequestDto createRequest(String username, StorageRequestDto requestDto, 
                                          String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already has a pending request
        if (storageRequestRepository.existsByUserAndStatus(user, StorageRequest.Status.PENDING)) {
            throw new RuntimeException("You already have a pending storage request");
        }

        // Validate requested quota is greater than current
        long currentQuotaGb = user.getStorageQuota() / (1024 * 1024 * 1024);
        if (requestDto.getRequestedQuotaGb() <= currentQuotaGb) {
            throw new RuntimeException("Requested quota must be greater than your current quota of " + currentQuotaGb + " GB");
        }

        StorageRequest request = new StorageRequest();
        request.setUser(user);
        request.setRequestedQuotaGb(requestDto.getRequestedQuotaGb());
        request.setReason(requestDto.getReason());
        request.setStatus(StorageRequest.Status.PENDING);
        request.setPreviousQuotaGb((int) currentQuotaGb);

        StorageRequest saved = storageRequestRepository.save(request);

        // Log the action
        auditLogService.logAction(
                user.getId(),
                username,
                "STORAGE_REQUEST_CREATE",
                "STORAGE_REQUEST",
                saved.getId(),
                ipAddress,
                userAgent,
                "SUCCESS",
                "Requested " + requestDto.getRequestedQuotaGb() + " GB storage"
        );

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<StorageRequestDto> getUserRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return storageRequestRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<StorageRequestDto> getAllRequests(String status, Pageable pageable) {
        Page<StorageRequest> requests;
        
        if (status != null && !status.isEmpty()) {
            requests = storageRequestRepository.findByStatusOrderByCreatedAtDesc(
                    StorageRequest.Status.valueOf(status), pageable);
        } else {
            requests = storageRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        return requests.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public long getPendingRequestCount() {
        return storageRequestRepository.countByStatus(StorageRequest.Status.PENDING);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StorageRequestDto approveRequest(Long requestId, ApproveStorageRequest approvalDto,
                                            String adminUsername, String ipAddress, String userAgent) {
        StorageRequest request = storageRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Storage request not found"));

        if (request.getStatus() != StorageRequest.Status.PENDING) {
            throw new RuntimeException("Request has already been processed");
        }

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        User user = request.getUser();

        if (approvalDto.getApproved()) {
            // Approve the request
            request.setStatus(StorageRequest.Status.APPROVED);
            request.setApprovedBy(admin);
            request.setApprovedAt(LocalDateTime.now());
            request.setAdminNotes(approvalDto.getAdminNotes());

            // Update user storage quota
            int approvedQuotaGb = approvalDto.getApprovedQuotaGb() != null 
                    ? approvalDto.getApprovedQuotaGb() 
                    : request.getRequestedQuotaGb();
            
            long newQuotaBytes = (long) approvedQuotaGb * 1024 * 1024 * 1024;
            user.setStorageQuota(newQuotaBytes);
            userRepository.save(user);

            auditLogService.logAction(
                    admin.getId(),
                    adminUsername,
                    "STORAGE_REQUEST_APPROVE",
                    "STORAGE_REQUEST",
                    request.getId(),
                    ipAddress,
                    userAgent,
                    "SUCCESS",
                    "Approved storage request for " + user.getUsername() + " - " + approvedQuotaGb + " GB"
            );
        } else {
            // Reject the request
            request.setStatus(StorageRequest.Status.REJECTED);
            request.setApprovedBy(admin);
            request.setApprovedAt(LocalDateTime.now());
            request.setAdminNotes(approvalDto.getAdminNotes());

            auditLogService.logAction(
                    admin.getId(),
                    adminUsername,
                    "STORAGE_REQUEST_REJECT",
                    "STORAGE_REQUEST",
                    request.getId(),
                    ipAddress,
                    userAgent,
                    "SUCCESS",
                    "Rejected storage request for " + user.getUsername()
            );
        }

        StorageRequest saved = storageRequestRepository.save(request);
        return mapToDto(saved);
    }

    @Transactional
    public void cancelRequest(Long requestId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StorageRequest request = storageRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Storage request not found"));

        // Only allow cancellation of own pending requests
        if (!request.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only cancel your own requests");
        }

        if (request.getStatus() != StorageRequest.Status.PENDING) {
            throw new RuntimeException("Can only cancel pending requests");
        }

        storageRequestRepository.delete(request);

        // L6: Audit log for cancellation (previously missing)
        auditLogService.logAction(
                user.getId(), username,
                "STORAGE_REQUEST_CANCEL", "STORAGE_REQUEST", requestId,
                null, null, "SUCCESS",
                "Cancelled storage request for " + request.getRequestedQuotaGb() + " GB"
        );
    }

    private StorageRequestDto mapToDto(StorageRequest request) {
        return StorageRequestDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .userEmail(request.getUser().getEmail())
                .userFullName(request.getUser().getFullName())
                .userRole(request.getUser().getRole().toString())
                .requestedQuotaGb(request.getRequestedQuotaGb())
                .reason(request.getReason())
                .adminNotes(request.getAdminNotes())
                .status(request.getStatus().toString())
                .approvedBy(request.getApprovedBy() != null ? request.getApprovedBy().getUsername() : null)
                .approvedAt(request.getApprovedAt())
                .previousQuotaGb(request.getPreviousQuotaGb())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
