package com.udom.securecloud.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    
    // Endpoints to exclude from logging (sensitive operations)
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/refresh",
            "/api/auth/change-password",
            "/api/auth/2fa/setup",
            "/api/auth/2fa/enable",
            "/api/auth/2fa/disable",
            "/api/admin/users/reset-password"
    ));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip logging for excluded paths
        if (shouldExcludePath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        
        // Wrap request and response to cache content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log request details
            logRequest(wrappedRequest, duration);
            
            // Log response details
            logResponse(wrappedResponse, duration);
            
            // Copy response content back to original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, long duration) {
        try {
            String method = request.getMethod();
            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String contentType = request.getContentType();
            String userAgent = request.getHeader("User-Agent");
            String clientIp = getClientIp(request);
            
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("REQUEST: ")
                    .append(method).append(" ").append(path);
            
            if (queryString != null && !queryString.isEmpty()) {
                logMessage.append("?").append(queryString);
            }
            
            logMessage.append(" | IP: ").append(clientIp)
                    .append(" | ContentType: ").append(contentType)
                    .append(" | UserAgent: ").append(userAgent);
            
            logger.info(logMessage.toString());
            
            // Log request body for POST/PUT requests (first 1000 chars)
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                byte[] content = request.getContentAsByteArray();
                if (content.length > 0) {
                    String body = new String(content);
                    if (body.length() > 1000) {
                        body = body.substring(0, 1000) + "...";
                    }
                    logger.debug("REQUEST_BODY: {}", body);
                }
            }
        } catch (Exception e) {
            logger.error("Error logging request", e);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        try {
            int status = response.getStatus();
            String contentType = response.getContentType();
            int contentLength = response.getContentSize();
            
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("RESPONSE: ")
                    .append(status)
                    .append(" | ContentType: ").append(contentType)
                    .append(" | Size: ").append(contentLength).append(" bytes")
                    .append(" | Duration: ").append(duration).append("ms");
            
            // Log at different levels based on status code
            if (status >= 500) {
                logger.error(logMessage.toString());
            } else if (status >= 400) {
                logger.warn(logMessage.toString());
            } else {
                logger.info(logMessage.toString());
            }
            
            // Log response body for errors (first 1000 chars)
            if (status >= 400) {
                byte[] content = response.getContentAsByteArray();
                if (content.length > 0) {
                    String body = new String(content);
                    if (body.length() > 1000) {
                        body = body.substring(0, 1000) + "...";
                    }
                    logger.debug("RESPONSE_BODY: {}", body);
                }
            }
        } catch (Exception e) {
            logger.error("Error logging response", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean shouldExcludePath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Don't filter actuator endpoints, swagger, etc.
        return path.startsWith("/actuator") || 
               path.startsWith("/swagger") || 
               path.startsWith("/api-docs") ||
               path.startsWith("/v3/api-docs");
    }
}
