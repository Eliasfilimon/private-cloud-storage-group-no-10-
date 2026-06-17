package com.udom.securecloud.controller;

import com.udom.securecloud.dto.FileResponse;
import com.udom.securecloud.dto.FileUploadResponse;
import com.udom.securecloud.service.FileStorageService;
import com.udom.securecloud.security.RateLimited;
import com.udom.securecloud.security.validation.FileUploadValidator;
import com.udom.securecloud.security.validation.FileUploadValidationException;
import com.udom.securecloud.validation.ValidFileName;
import com.udom.securecloud.validation.ValidationConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileUploadValidator fileUploadValidator;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    @RateLimited("upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "encrypt", required = false, defaultValue = "true") Boolean encrypt,
            @RequestParam(value = "folderId", required = false) Long folderId,
            HttpServletRequest request) {

        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            fileUploadValidator.validateFile(file, username);
            FileUploadResponse response = fileStorageService.uploadFile(file, username, encrypt, folderId, ipAddress, userAgent);
            return ResponseEntity.ok(response);
        } catch (FileUploadValidationException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("File validation failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileResponse>> getUserFiles(
            @RequestParam(value = "folderId", required = false) 
            @Positive(message = "Folder ID must be positive")
            Long folderId,
            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        List<FileResponse> files = fileStorageService.getUserFiles(username, folderId);
        return ResponseEntity.ok(files);
    }

    /** Gap 3 — Backend-powered search */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @RateLimited("search")
    public ResponseEntity<List<FileResponse>> searchFiles(
            @RequestParam("q") 
            @NotBlank(message = "Search query cannot be empty")
            @Size(min = 1, max = 100, message = "Search query must be 1-100 characters")
            @Pattern(regexp = ValidationConstants.SEARCH_PATTERN, 
                     message = ValidationConstants.SEARCH_MESSAGE)
            String query,
            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        List<FileResponse> results = fileStorageService.searchFiles(username, query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{fileId}/download")
    @PreAuthorize("isAuthenticated()")
    @RateLimited("download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable 
            @Positive(message = "File ID must be positive")
            Long fileId, 
            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        Resource resource = fileStorageService.downloadFile(fileId, username, ipAddress, userAgent);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /** Gap 5 — In-browser preview: decrypts and streams with correct Content-Type */
    @GetMapping("/{fileId}/preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> previewFile(
            @PathVariable 
            @Positive(message = "File ID must be positive")
            Long fileId, 
            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        FileStorageService.PreviewResult result = fileStorageService.previewFile(fileId, username, ipAddress, userAgent);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + result.getOriginalName() + "\"")
                .body(result.getResource());
    }

    @PutMapping("/{fileId}/rename")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileResponse> renameFile(
            @PathVariable 
            @Positive(message = "File ID must be positive")
            Long fileId,
            @RequestParam("name") 
            @NotBlank(message = "File name is required")
            @ValidFileName(message = "Invalid file name")
            String newName,
            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        FileResponse response = fileStorageService.renameFile(fileId, username, newName);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{fileId}/move")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileResponse> moveFile(
            @PathVariable 
            @Positive(message = "File ID must be positive")
            Long fileId,
            @RequestParam(value = "folderId", required = false) 
            @Positive(message = "Folder ID must be positive")
            Long targetFolderId,
            HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        FileResponse response = fileStorageService.moveFile(fileId, username, targetFolderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFile(
            @PathVariable 
            @Positive(message = "File ID must be positive")
            Long fileId, 
            HttpServletRequest request) {
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

    public static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
    }
}
