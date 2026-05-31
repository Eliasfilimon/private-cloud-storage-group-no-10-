package com.udom.securecloud.controller;

import com.udom.securecloud.dto.FileResponse;
import com.udom.securecloud.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TrashController {

    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<FileResponse>> getTrashedFiles(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        List<FileResponse> files = fileStorageService.getTrashedFiles(username);
        return ResponseEntity.ok(files);
    }

    @PutMapping("/{fileId}/restore")
    public ResponseEntity<Map<String, String>> restoreFile(@PathVariable Long fileId,
                                                            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        fileStorageService.restoreFile(fileId, username, ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "File restored successfully"));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> permanentDelete(@PathVariable Long fileId,
                                                                HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        fileStorageService.permanentDeleteFile(fileId, username, ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "File permanently deleted"));
    }

    @DeleteMapping("/empty")
    public ResponseEntity<Map<String, String>> emptyTrash(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        fileStorageService.emptyTrash(username, ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "Trash emptied successfully"));
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
