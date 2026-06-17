package com.udom.securecloud.repository;

import com.udom.securecloud.model.Session;
import com.udom.securecloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    /**
     * Find active session by token
     */
    Optional<Session> findByTokenAndIsActiveTrue(String token);
    
    /**
     * Find active session by refresh token
     */
    Optional<Session> findByRefreshTokenAndIsActiveTrue(String refreshToken);
    
    /**
     * Find all active sessions for a user
     */
    List<Session> findByUserAndIsActiveTrue(User user);
    
    /**
     * Find all sessions for a user
     */
    List<Session> findByUser(User user);
    
    /**
     * Invalidate all active sessions for a user
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.user = :user AND s.isActive = true")
    void invalidateAllUserSessions(@Param("user") User user);
    
    /**
     * Invalidate a specific session
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.token = :token")
    void invalidateSession(@Param("token") String token);
    
    /**
     * Update last activity timestamp
     */
    @Modifying
    @Query("UPDATE Session s SET s.lastActivity = :lastActivity WHERE s.token = :token")
    void updateLastActivity(@Param("token") String token, @Param("lastActivity") LocalDateTime lastActivity);
    
    /**
     * Delete expired sessions
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now OR (s.isActive = false AND s.createdAt < :cutoffTime)")
    void deleteExpiredSessions(@Param("now") LocalDateTime now, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Count active sessions for a user
     */
    long countByUserAndIsActiveTrue(User user);
}
