package com.udom.securecloud.controller;

import com.udom.securecloud.dto.CreateFolderRequest;
import com.udom.securecloud.dto.FolderResponse;
import com.udom.securecloud.dto.RenameFolderRequest;
import com.udom.securecloud.service.FolderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.udom.securecloud.repository.UserRepository;
import com.udom.securecloud.model.User;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody CreateFolderRequest request) {
        Long userId = getCurrentUserId();
        FolderResponse response = folderService.createFolder(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FolderResponse>> getRootFolders() {
        Long userId = getCurrentUserId();
        List<FolderResponse> folders = folderService.getRootFolders(userId);
        return ResponseEntity.ok(folders);
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse> getFolder(
            @PathVariable 
            @Positive(message = "Folder ID must be positive")
            Long folderId) {
        Long userId = getCurrentUserId();
        FolderResponse folder = folderService.getFolder(folderId, userId);
        return ResponseEntity.ok(folder);
    }

    @GetMapping("/{folderId}/subfolders")
    public ResponseEntity<List<FolderResponse>> getSubFolders(
            @PathVariable 
            @Positive(message = "Folder ID must be positive")
            Long folderId) {
        Long userId = getCurrentUserId();
        List<FolderResponse> folders = folderService.getSubFolders(folderId, userId);
        return ResponseEntity.ok(folders);
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<FolderResponse> updateFolder(
            @PathVariable 
            @Positive(message = "Folder ID must be positive")
            Long folderId,
            @Valid @RequestBody RenameFolderRequest request) {
        Long userId = getCurrentUserId();
        FolderResponse response = folderService.updateFolder(folderId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable 
            @Positive(message = "Folder ID must be positive")
            Long folderId) {
        Long userId = getCurrentUserId();
        folderService.deleteFolder(folderId, userId);
        return ResponseEntity.ok().build();
    }
}
