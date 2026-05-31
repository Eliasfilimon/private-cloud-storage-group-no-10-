package com.udom.securecloud.controller;

import com.udom.securecloud.dto.FileVersionResponse;
import com.udom.securecloud.service.FileVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileVersionController {

    private final FileVersionService fileVersionService;

    @GetMapping("/{fileId}/versions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileVersionResponse>> getVersions(
            @PathVariable Long fileId,
            Principal principal) {
        return ResponseEntity.ok(fileVersionService.getVersions(fileId, principal.getName()));
    }

    @PostMapping("/{fileId}/versions/{versionId}/restore")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> restoreVersion(
            @PathVariable Long fileId,
            @PathVariable Long versionId,
            Principal principal) {
        fileVersionService.restoreVersion(fileId, versionId, principal.getName());
        return ResponseEntity.ok().build();
    }
}
