package com.udom.securecloud.controller;

import com.udom.securecloud.dto.UserResponse;
import com.udom.securecloud.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public-facing user endpoints accessible to any authenticated user.
 * Used primarily for sharing functionality where users need to list other users.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsersForSharing();
        return ResponseEntity.ok(users);
    }
}
