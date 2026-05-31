package com.udom.securecloud.repository;

import com.udom.securecloud.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Long countByUserIdAndActionAndCreatedAtAfter(Long userId, String action, LocalDateTime after);
    
    Long countByActionAndCreatedAtAfter(String action, LocalDateTime after);
    
    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    Page<AuditLog> findByAction(String action, Pageable pageable);
}
