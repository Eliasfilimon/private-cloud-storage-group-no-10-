package com.udom.securecloud.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateLimitingFilter: Token bucket algorithm for rate limiting.
 * 
 * Per-Endpoint Limits:
 * - /api/auth/login: 5 attempts/minute per IP
 * - /api/auth/register: 3 attempts/minute per IP/hiini out of context for future if user register themselfs
 * - /api/files/upload: 20 per minute per user
 * - /api/files/download: 100 per minute per user
 * - /api/auth/password-reset: 3 per hour per IP
 * 
 * Returns HTTP 429 Too Many Requests when limit exceeded.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    // Token buckets: key -> (tokens, lastRefillTime)
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    // Rate limit configurations per endpoint
    private static final Map<String, RateLimitConfig> RATE_LIMITS = new HashMap<>();
    
    static {
        RATE_LIMITS.put("/api/auth/login", new RateLimitConfig(5, 60));              // 5 per minute per IP
        RATE_LIMITS.put("/api/auth/register", new RateLimitConfig(3, 60));           // 3 per minute per IP
        RATE_LIMITS.put("/api/files/upload", new RateLimitConfig(20, 60));           // 20 per minute per user
        RATE_LIMITS.put("/api/files/download", new RateLimitConfig(100, 60));        // 100 per minute per user
        RATE_LIMITS.put("/api/auth/password-reset", new RateLimitConfig(3, 3600));   // 3 per hour per IP
        RATE_LIMITS.put("/api/files/delete", new RateLimitConfig(30, 60));           // 30 per minute per user
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Find matching rate limit config
        RateLimitConfig config = null;
        for (String endpoint : RATE_LIMITS.keySet()) {
            if (path.startsWith(endpoint)) {
                config = RATE_LIMITS.get(endpoint);
                break;
            }
        }
        
        // If no specific rate limit found, allow request
        if (config == null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Determine rate limit key (IP or user)
        String limitKey = getLimitKey(request, path);
        
        // Check rate limit
        if (!checkRateLimit(limitKey, config)) {
            logger.warn("Rate limit exceeded - Key: {} - Path: {}", limitKey, path);
            
            response.setStatus(429); // HTTP_TOO_MANY_REQUESTS
            response.setHeader("Retry-After", String.valueOf(config.windowSeconds));
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Get the rate limit key based on endpoint type
     */
    private String getLimitKey(HttpServletRequest request, String path) {
        String clientIp = getClientIp(request);
        
        // User-based limits
        if (path.startsWith("/api/files/")) {
            String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
            return "user:" + username + ":" + path;
        }
        
        // IP-based limits (auth endpoints)
        return "ip:" + clientIp + ":" + path;
    }
    
    /**
     * Check if request is within rate limit
     */
    private boolean checkRateLimit(String key, RateLimitConfig config) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(config.maxTokens, config.windowSeconds));
        
        return bucket.allowRequest();
    }
    
    /**
     * Get client IP address (respects X-Forwarded-For header)
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
    
    /**
     * Token Bucket implementation
     */
    private static class TokenBucket {
        private final long maxTokens;
        private final double refillRatePerSecond;
        private double tokens;
        private long lastRefillTime;
        
        TokenBucket(long maxTokens, long windowSeconds) {
            this.maxTokens = maxTokens;
            this.refillRatePerSecond = maxTokens / (double) windowSeconds;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        synchronized boolean allowRequest() {
            // Refill tokens based on elapsed time
            long currentTime = System.currentTimeMillis();
            long elapsedSeconds = (currentTime - lastRefillTime) / 1000;
            
            if (elapsedSeconds > 0) {
                tokens = Math.min((double) maxTokens, tokens + (elapsedSeconds * refillRatePerSecond));
                lastRefillTime = currentTime;
            }
            
            // Check if we can fulfill this request
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Rate limit configuration
     */
    private static class RateLimitConfig {
        long maxTokens;
        long windowSeconds;
        
        RateLimitConfig(long maxTokens, long windowSeconds) {
            this.maxTokens = maxTokens;
            this.windowSeconds = windowSeconds;
        }
    }
}
