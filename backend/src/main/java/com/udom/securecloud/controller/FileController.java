package com.udom.securecloud.controller;

import com.udom.securecloud.dto.FileResponse;
import com.udom.securecloud.dto.FileUploadResponse;
import com.udom.securecloud.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "encrypt", required = false, defaultValue = "true") Boolean encrypt,
            HttpServletRequest request) {
        
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        FileUploadResponse response = fileStorageService.uploadFile(file, username, encrypt, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileResponse>> getUserFiles(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        List<FileResponse> files = fileStorageService.getUserFiles(username);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{fileId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        Resource resource = fileStorageService.downloadFile(fileId, username, ipAddress, userAgent);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        fileStorageService.deleteFile(fileId, username, ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
