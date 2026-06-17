package com.udom.securecloud.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Value("${security.headers.hsts.enabled:true}")
    private boolean hstsEnabled;

    @Value("${security.headers.hsts.max-age:31536000}")
    private long hstsMaxAge;

    @Value("${security.headers.hsts.include-subdomains:true}")
    private boolean hstsIncludeSubdomains;

    @Value("${security.headers.csp.enabled:true}")
    private boolean cspEnabled;

    @Value("${security.headers.csp.policy:default-src 'self'}")
    private String cspPolicy;

    @Value("${security.headers.permissions-policy.enabled:true}")
    private boolean permissionsPolicyEnabled;

    @Value("${security.headers.permissions-policy.policy:geolocation=(), microphone=(), camera=()}")
    private String permissionsPolicy;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Add HSTS header (HTTP Strict Transport Security)
        if (hstsEnabled) {
            StringBuilder hstsValue = new StringBuilder();
            hstsValue.append("max-age=").append(hstsMaxAge);
            if (hstsIncludeSubdomains) {
                hstsValue.append("; includeSubDomains");
            }
            hstsValue.append("; preload");
            response.setHeader("Strict-Transport-Security", hstsValue.toString());
        }

        // Add CSP header (Content Security Policy)
        if (cspEnabled && !cspPolicy.isEmpty()) {
            response.setHeader("Content-Security-Policy", cspPolicy);
        }

        // Add Permissions-Policy header (formerly Feature-Policy)
        if (permissionsPolicyEnabled && !permissionsPolicy.isEmpty()) {
            response.setHeader("Permissions-Policy", permissionsPolicy);
        }

        // Add X-Permitted-Cross-Domain-Policies header
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");

        // Add X-Content-Type-Options header (prevent MIME sniffing)
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Add X-Frame-Options header (prevent clickjacking)
        response.setHeader("X-Frame-Options", "DENY");

        // Add X-XSS-Protection header (enable XSS filter in browsers)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Add Referrer-Policy header
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Add API Version header
        response.setHeader("X-API-Version", "v1");

        filterChain.doFilter(request, response);
    }
}
