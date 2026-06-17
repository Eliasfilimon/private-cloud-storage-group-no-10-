package com.udom.securecloud.service;

import com.udom.securecloud.model.Session;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SessionService: Manages server-side session tracking
 * Handles session creation, validation, and cleanup
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    
    private final SessionRepository sessionRepository;
    
    @Value("${session.timeout-minutes:30}")
    private int sessionTimeoutMinutes;
    
    @Value("${jwt.expiration:900000}")
    private long jwtExpirationMs;
    
    /**
     * Create a new session
     */
    public Session createSession(User user, String token, String refreshToken, String ipAddress, String userAgent) {
        logger.debug("Creating session for user: {}", user.getUsername());
        
        Session session = Session.builder()
                .user(user)
                .token(token)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .lastActivity(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes))
                .isActive(true)
                .build();
        
        return sessionRepository.save(session);
    }
    
    /**
     * Validate session
     */
    public boolean validateSession(String token) {
        Optional<Session> sessionOpt = sessionRepository.findByTokenAndIsActiveTrue(token);
        
        if (sessionOpt.isEmpty()) {
            logger.warn("Session not found or inactive for token: {}", token);
            return false;
        }
        
        Session session = sessionOpt.get();
        
        // Check if session has expired
        if (!session.isValid()) {
            logger.warn("Session expired for user: {}", session.getUser().getUsername());
            return false;
        }
        
        // Check if session has timed out due to inactivity
        if (session.hasTimedOut(sessionTimeoutMinutes)) {
            logger.warn("Session timed out due to inactivity for user: {}", session.getUser().getUsername());
            invalidateSession(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get session by token
     */
    public Optional<Session> getSession(String token) {
        return sessionRepository.findByTokenAndIsActiveTrue(token);
    }
    
    /**
     * Update last activity timestamp
     */
    @Transactional
    public void updateActivity(String token) {
        try {
            sessionRepository.updateLastActivity(token, LocalDateTime.now());
            logger.debug("Updated activity for token: {}", token);
        } catch (Exception e) {
            logger.warn("Failed to update activity for token: {}", e.getMessage());
        }
    }
    
    /**
     * Invalidate a specific session
     */
    public void invalidateSession(String token) {
        sessionRepository.invalidateSession(token);
        logger.info("Invalidated session for token: {}", token);
    }
    
    /**
     * Invalidate all sessions for a user
     */
    public void invalidateAllUserSessions(User user) {
        sessionRepository.invalidateAllUserSessions(user);
        logger.info("Invalidated all sessions for user: {}", user.getUsername());
    }
    
    /**
     * Check if refresh token is valid
     */
    public Optional<Session> validateRefreshToken(String refreshToken) {
        Optional<Session> sessionOpt = sessionRepository.findByRefreshTokenAndIsActiveTrue(refreshToken);
        
        if (sessionOpt.isEmpty()) {
            logger.warn("Refresh token not found or inactive");
            return Optional.empty();
        }
        
        Session session = sessionOpt.get();
        
        if (!session.isValid()) {
            logger.warn("Refresh token expired for user: {}", session.getUser().getUsername());
            return Optional.empty();
        }
        
        return Optional.of(session);
    }
    
    /**
     * Cleanup expired sessions (runs every hour)
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void cleanupExpiredSessions() {
        logger.info("Cleaning up expired sessions");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusDays(7); // Keep inactive sessions for 7 days
        sessionRepository.deleteExpiredSessions(now, cutoffTime);
    }
}
