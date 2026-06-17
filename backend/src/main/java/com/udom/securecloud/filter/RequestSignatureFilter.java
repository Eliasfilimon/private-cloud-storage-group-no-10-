package com.udom.securecloud.filter;

import com.udom.securecloud.exception.RateLimitException;
import com.udom.securecloud.security.RequestSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class RequestSignatureFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestSignatureFilter.class);

    // Endpoints that require signature validation (sensitive operations)
    private static final Set<String> SIGNATURE_REQUIRED_PATHS = new HashSet<>(Arrays.asList(
            "/api/files/upload",
            "/api/files/delete",
            "/api/admin/users",
            "/api/admin/users/bulk-upload",
            "/api/admin/users/reset-password",
            "/api/shares/files"
    ));

    private final RequestSignatureValidator signatureValidator;

    public RequestSignatureFilter(RequestSignatureValidator signatureValidator) {
        this.signatureValidator = signatureValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Only validate state-changing requests (POST, PUT, DELETE)
        if (!isStateChangingRequest(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Only validate sensitive endpoints
        if (!requiresSignatureValidation(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip validation if disabled
        if (!signatureValidator.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract signature and timestamp from headers
        String signature = request.getHeader("X-Request-Signature");
        String timestamp = request.getHeader("X-Request-Timestamp");

        // Validate signature
        if (!signatureValidator.validateRequest(method, path, signature, timestamp)) {
            String errorMessage = signatureValidator.getValidationError(method, path, signature, timestamp);
            logger.warn("Request signature validation failed for {}: {}", path, errorMessage);
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid request signature\",\"message\":\"" + errorMessage + "\"}");
            return;
        }

        logger.debug("Request signature validated for {}", path);
        filterChain.doFilter(request, response);
    }

    private boolean isStateChangingRequest(String method) {
        return "POST".equalsIgnoreCase(method) || 
               "PUT".equalsIgnoreCase(method) || 
               "DELETE".equalsIgnoreCase(method);
    }

    private boolean requiresSignatureValidation(String path) {
        return SIGNATURE_REQUIRED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Don't filter non-API endpoints
        return !path.startsWith("/api/");
    }
}
