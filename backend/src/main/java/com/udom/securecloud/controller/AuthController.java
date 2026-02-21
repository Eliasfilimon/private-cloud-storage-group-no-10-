package com.udom.securecloud.controller;

import com.udom.securecloud.dto.AuthResponse;
import com.udom.securecloud.dto.CreateUserRequest;
import com.udom.securecloud.dto.LoginRequest;
import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                             HttpServletRequest request) {
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        AuthResponse response = authService.login(loginRequest, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest createUserRequest,
                                                 HttpServletRequest request) {
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        UserResponse response = authService.createUser(createUserRequest, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String token,
                                                       HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        UserResponse response = authService.getCurrentUser(username);
        return ResponseEntity.ok(response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
