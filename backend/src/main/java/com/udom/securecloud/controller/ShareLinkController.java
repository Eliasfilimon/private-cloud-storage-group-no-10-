package com.udom.securecloud.controller;

import com.udom.securecloud.dto.CreateShareLinkRequest;
import com.udom.securecloud.dto.ShareLinkResponse;
import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.service.FileStorageService;
import com.udom.securecloud.service.ShareLinkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ShareLinkController {

    private final ShareLinkService shareLinkService;
    private final FileStorageService fileStorageService;

    // ---------- Authenticated endpoints ----------

    @PostMapping("/api/links")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShareLinkResponse> createLink(
            @RequestBody CreateShareLinkRequest request,
            Principal principal) {
        return ResponseEntity.ok(shareLinkService.createLink(request, principal.getName()));
    }

    @GetMapping("/api/links/files/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShareLinkResponse>> getLinksByFile(
            @PathVariable Long fileId,
            Principal principal) {
        return ResponseEntity.ok(shareLinkService.getLinksByFile(fileId, principal.getName()));
    }

    @DeleteMapping("/api/links/{token}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> revokeLink(
            @PathVariable String token,
            Principal principal) {
        shareLinkService.revokeLink(token, principal.getName());
        return ResponseEntity.ok().build();
    }

    // ---------- Public endpoints (no auth) ----------

    @GetMapping("/public/share/{token}")
    public ResponseEntity<Map<String, Object>> getShareInfo(
            @PathVariable String token,
            @RequestParam(required = false) String password) {
        FileMetadata file = shareLinkService.resolveToken(token, password);
        return ResponseEntity.ok(Map.of(
                "fileName", file.getOriginalName(),
                "fileSize", file.getFileSize(),
                "mimeType", file.getMimeType()
        ));
    }

    @GetMapping("/public/share/{token}/download")
    public ResponseEntity<InputStreamResource> downloadViaLink(
            @PathVariable String token,
            @RequestParam(required = false) String password,
            HttpServletRequest request) {
        FileMetadata file = shareLinkService.resolveToken(token, password);
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        org.springframework.core.io.Resource resource =
                fileStorageService.downloadFile(file.getId(), file.getUser().getUsername(), ip, ua);

        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getOriginalName() + "\"")
                    .contentType(MediaType.parseMediaType(
                            file.getMimeType() != null ? file.getMimeType() : "application/octet-stream"))
                    .body(new InputStreamResource(resource.getInputStream()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
