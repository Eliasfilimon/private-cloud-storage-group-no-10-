package com.udom.securecloud.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Session: Tracks user sessions for server-side session management
 * Enables timeout enforcement and activity tracking
 */
@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_sessions_user_id", columnList = "user_id"),
    @Index(name = "idx_sessions_token", columnList = "token"),
    @Index(name = "idx_sessions_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "token", nullable = false, unique = true, length = 1000)
    private String token;
    
    @Column(name = "refresh_token", nullable = false, unique = true, length = 1000)
    private String refreshToken;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }
    
    /**
     * Check if session is still valid
     * Note: expiresAt is kept for refresh token expiration only
     * Session validity is determined by inactivity timeout, not fixed expiration
     */
    public boolean isValid() {
        return isActive;
    }
    
    /**
     * Check if session has timed out due to inactivity
     * @param inactivityTimeoutMinutes timeout in minutes
     */
    public boolean hasTimedOut(int inactivityTimeoutMinutes) {
        LocalDateTime timeoutTime = lastActivity.plusMinutes(inactivityTimeoutMinutes);
        return LocalDateTime.now().isAfter(timeoutTime);
    }
}
