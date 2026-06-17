package com.udom.securecloud.interceptor;

import com.udom.securecloud.config.ApiVersionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiVersionInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ApiVersionInterceptor.class);
    private final ApiVersionConfig versionConfig;

    public ApiVersionInterceptor(ApiVersionConfig versionConfig) {
        this.versionConfig = versionConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        String requestPath = request.getRequestURI();
        
        // Only process API requests
        if (!requestPath.startsWith("/api/")) {
            return true;
        }

        // Extract version from path (e.g., /api/v1/files -> v1)
        String version = extractVersionFromPath(requestPath);
        
        // If no version in path, check header
        if (version == null) {
            version = request.getHeader("X-API-Version");
        }
        
        // Default to current version if not specified
        if (version == null) {
            version = versionConfig.getCurrentVersion();
        }

        // Check if version is deprecated
        if (versionConfig.isVersionDeprecated(version)) {
            logger.warn("Deprecated API version used: {} from IP: {}", version, getClientIp(request));
            response.setHeader("X-API-Deprecated", "true");
            response.setHeader("X-API-Sunset", "Please upgrade to " + versionConfig.getCurrentVersion());
        }

        // Add version to response header
        if (versionConfig.isVersionHeaderEnabled()) {
            response.setHeader("X-API-Version", version);
        }

        // Store version in request for later use
        request.setAttribute("api.version", version);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) throws Exception {
        // Log API version usage
        String version = (String) request.getAttribute("api.version");
        if (version != null) {
            logger.debug("API request completed with version: {}", version);
        }
    }

    private String extractVersionFromPath(String path) {
        // Pattern: /api/v1/... or /api/v2/...
        String[] parts = path.split("/");
        
        if (parts.length >= 3) {
            String potentialVersion = parts[2];
            if (potentialVersion.matches("v\\d+")) {
                return potentialVersion;
            }
        }
        
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
