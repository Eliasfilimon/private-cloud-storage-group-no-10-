package com.udom.securecloud.security;

import com.udom.securecloud.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * SessionValidationFilter: Validates server-side sessions on each request
 * Ensures user sessions haven't timed out due to inactivity
 */
@Component
@RequiredArgsConstructor
public class SessionValidationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionValidationFilter.class);
    
    private final SessionService sessionService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                // Validate JWT token
                if (!jwtTokenProvider.validateToken(token)) {
                    logger.warn("Invalid JWT token");
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
                
                // Validate server-side session
                if (!sessionService.validateSession(token)) {
                    logger.warn("Session validation failed for token");
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or invalid");
                    return;
                }
                
                // Update last activity to extend session timeout
                sessionService.updateActivity(token);
            }
        } catch (Exception e) {
            logger.error("Error validating session: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
