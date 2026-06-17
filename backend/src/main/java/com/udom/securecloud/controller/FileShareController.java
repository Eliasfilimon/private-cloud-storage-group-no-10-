package com.udom.securecloud.controller;

import com.udom.securecloud.dto.ShareFileRequest;
import com.udom.securecloud.dto.SharedFileResponse;
import com.udom.securecloud.service.FileShareService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class FileShareController {

    private final FileShareService fileShareService;

    @PostMapping("/files/{fileId}")
    public ResponseEntity<?> shareFile(
            @PathVariable 
            @Positive(message = "File ID must be positive")
            Long fileId,
            @RequestBody 
            @Valid
            ShareFileRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            String username = authentication.getName();
            String ipAddress = httpRequest.getRemoteAddr();
            String userAgent = httpRequest.getHeader("User-Agent");

            List<SharedFileResponse> responses = fileShareService.shareFile(
                    fileId, username, request, ipAddress, userAgent);
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<?> getFilesSharedWithMe(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<SharedFileResponse> files = fileShareService.getFilesSharedWithMe(username);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/shared-by-me")
    public ResponseEntity<?> getFilesSharedByMe(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<SharedFileResponse> files = fileShareService.getFilesSharedByMe(username);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<?> getFileShares(
            @PathVariable 
            @Positive(message = "File ID must be positive")
            Long fileId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            List<SharedFileResponse> shares = fileShareService.getFileShares(fileId, username);
            return ResponseEntity.ok(shares);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> unshareFile(
            @PathVariable 
            @Positive(message = "Share ID must be positive")
            Long shareId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            String username = authentication.getName();
            String ipAddress = httpRequest.getRemoteAddr();
            String userAgent = httpRequest.getHeader("User-Agent");

            fileShareService.unshareFile(shareId, username, ipAddress, userAgent);
            return ResponseEntity.ok("File unshared successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/can-access/{fileId}")
    public ResponseEntity<?> canAccessFile(
            @PathVariable 
            @Positive(message = "File ID must be positive")
            Long fileId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            boolean canAccess = fileShareService.canAccessFile(fileId, username);
            return ResponseEntity.ok(canAccess);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
