package com.udom.securecloud.service;

import com.udom.securecloud.dto.FileResponse;
import com.udom.securecloud.dto.ShareFileRequest;
import com.udom.securecloud.dto.SharedFileResponse;
import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.SharedFile;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.SharedFileRepository;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileShareService {

    private final SharedFileRepository sharedFileRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public List<SharedFileResponse> shareFile(Long fileId, String ownerUsername, 
                                             ShareFileRequest request, 
                                             String ipAddress, String userAgent) {
        // Verify file ownership
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        if (!file.getUserId().equals(owner.getId())) {
            throw new RuntimeException("You don't have permission to share this file");
        }

        List<SharedFileResponse> responses = new ArrayList<>();
        
        for (Long userId : request.getUserIds()) {
            // Check if user exists
            User sharedWithUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

            // Don't share with yourself
            if (userId.equals(owner.getId())) {
                continue;
            }

            // Check if already shared
            var existing = sharedFileRepository.findByFileIdAndSharedWithIdAndIsActiveTrue(fileId, userId);
            if (existing.isPresent()) {
                // Update permission if exists
                SharedFile existingShare = existing.get();
                existingShare.setPermission(SharedFile.Permission.valueOf(request.getPermission()));
                if (request.getExpiresInDays() != null) {
                    existingShare.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiresInDays()));
                }
                SharedFile updated = sharedFileRepository.save(existingShare);
                responses.add(toResponse(updated, file, owner, sharedWithUser));
            } else {
                // Create new share
                SharedFile sharedFile = new SharedFile();
                sharedFile.setFileId(fileId);
                sharedFile.setOwnerId(owner.getId());
                sharedFile.setOwnerUsername(owner.getUsername());
                sharedFile.setSharedWithId(userId);
                sharedFile.setSharedWithUsername(sharedWithUser.getUsername());
                sharedFile.setPermission(SharedFile.Permission.valueOf(request.getPermission()));
                sharedFile.setIsActive(true);
                
                if (request.getExpiresInDays() != null) {
                    sharedFile.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiresInDays()));
                }

                SharedFile saved = sharedFileRepository.save(sharedFile);
                responses.add(toResponse(saved, file, owner, sharedWithUser));

                // Log the action
                auditLogService.logAction(
                        owner.getId(),
                        owner.getUsername(),
                        "SHARE_FILE",
                        "FILE",
                        fileId,
                        ipAddress,
                        userAgent,
                        "SUCCESS",
                        "File shared with " + sharedWithUser.getUsername()
                );
            }
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<SharedFileResponse> getFilesSharedWithMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SharedFile> sharedFiles = sharedFileRepository.findBySharedWithIdAndIsActiveTrue(user.getId());
        
        return sharedFiles.stream()
                .filter(sf -> sf.getExpiresAt() == null || sf.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(sf -> {
                    FileMetadata file = fileMetadataRepository.findById(sf.getFileId()).orElse(null);
                    User owner = userRepository.findById(sf.getOwnerId()).orElse(null);
                    if (file != null && owner != null) {
                        return toResponse(sf, file, owner, user);
                    }
                    return null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SharedFileResponse> getFilesSharedByMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SharedFile> sharedFiles = sharedFileRepository.findByOwnerIdAndIsActiveTrue(user.getId());
        
        return sharedFiles.stream()
                .map(sf -> {
                    FileMetadata file = fileMetadataRepository.findById(sf.getFileId()).orElse(null);
                    User sharedWithUser = userRepository.findById(sf.getSharedWithId()).orElse(null);
                    if (file != null && sharedWithUser != null) {
                        return toResponse(sf, file, user, sharedWithUser);
                    }
                    return null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public void unshareFile(Long shareId, String username, String ipAddress, String userAgent) {
        SharedFile sharedFile = sharedFileRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only owner can unshare
        if (!sharedFile.getOwnerId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to unshare this file");
        }

        sharedFile.setIsActive(false);
        sharedFileRepository.save(sharedFile);

        // Log the action
        auditLogService.logAction(
                user.getId(),
                user.getUsername(),
                "UNSHARE_FILE",
                "FILE",
                sharedFile.getFileId(),
                ipAddress,
                userAgent,
                "SUCCESS",
                "File unshared from " + sharedFile.getSharedWithUsername()
        );
    }

    @Transactional(readOnly = true)
    public boolean canAccessFile(Long fileId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElse(null);

        if (file == null) {
            return false;
        }

        // Owner can always access
        if (file.getUserId().equals(user.getId())) {
            return true;
        }

        // Check if shared with user
        var share = sharedFileRepository.findByFileIdAndSharedWithIdAndIsActiveTrue(fileId, user.getId());
        if (share.isPresent()) {
            SharedFile sharedFile = share.get();
            // Check expiration
            if (sharedFile.getExpiresAt() != null && sharedFile.getExpiresAt().isBefore(LocalDateTime.now())) {
                return false;
            }
            return true;
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<SharedFileResponse> getFileShares(Long fileId, String username) {
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only owner can see all shares
        if (!file.getUserId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to view shares for this file");
        }

        List<SharedFile> shares = sharedFileRepository.findByFileIdAndIsActiveTrue(fileId);
        
        return shares.stream()
                .map(sf -> {
                    User sharedWithUser = userRepository.findById(sf.getSharedWithId()).orElse(null);
                    if (sharedWithUser != null) {
                        return toResponse(sf, file, user, sharedWithUser);
                    }
                    return null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    private SharedFileResponse toResponse(SharedFile sharedFile, FileMetadata file, User owner, User sharedWithUser) {
        return SharedFileResponse.builder()
                .id(sharedFile.getId())
                .fileId(file.getId())
                .fileName(file.getOriginalName())
                .fileType(file.getMimeType())
                .fileSize(file.getFileSize())
                .ownerId(owner.getId())
                .ownerUsername(owner.getUsername())
                .ownerFullName(owner.getFullName())
                .sharedWithId(sharedWithUser.getId())
                .sharedWithUsername(sharedWithUser.getUsername())
                .sharedWithFullName(sharedWithUser.getFullName())
                .permission(sharedFile.getPermission().toString())
                .isActive(sharedFile.getIsActive())
                .sharedAt(sharedFile.getSharedAt())
                .expiresAt(sharedFile.getExpiresAt())
                .lastAccessedAt(sharedFile.getLastAccessedAt())
                .build();
    }
}
