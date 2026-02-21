package com.udom.securecloud.service;

import com.udom.securecloud.model.AuditLog;
import com.udom.securecloud.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(Long userId, String username, String action, String resourceType,
                         Long resourceId, String ipAddress, String userAgent, 
                         String status, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setStatus(status);
        auditLog.setDetails(details);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void logAction(Long userId, String username, String action, String details) {
        logAction(userId, username, action, "USER", userId, null, null, "SUCCESS", details);
    }
}
