package com.udom.securecloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.udom.securecloud.model.AuditLog;
import com.udom.securecloud.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Long countByUserIdAndActionAndCreatedAtAfter(Long userId, String action, LocalDateTime after);
    
    Long countByActionAndCreatedAtAfter(String action, LocalDateTime after);
    
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
