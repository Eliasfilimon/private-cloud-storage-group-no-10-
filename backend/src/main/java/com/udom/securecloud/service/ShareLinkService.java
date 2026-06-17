package com.udom.securecloud.service;

import com.udom.securecloud.dto.CreateShareLinkRequest;
import com.udom.securecloud.dto.ShareLinkResponse;
import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.ShareLink;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.ShareLinkRepository;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareLinkService {

    private final ShareLinkRepository shareLinkRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** M3: Use the configurable frontend URL instead of hardcoded localhost. */
    @Value("${app.frontend-url:http://localhost:3002}")
    private String frontendUrl;

    @Transactional
    public ShareLinkResponse createLink(CreateShareLinkRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata file = fileMetadataRepository.findById(request.getFileId())
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't own this file");
        }

        String token = UUID.randomUUID().toString().replace("-", "");

        ShareLink link = ShareLink.builder()
                .token(token)
                .file(file)
                .createdBy(user)
                .expiresAt(request.getExpiresInDays() != null
                        ? LocalDateTime.now().plusDays(request.getExpiresInDays())
                        : null)
                .downloadLimit(request.getDownloadLimit())
                .downloadCount(0)
                .passwordHash(request.getPassword() != null && !request.getPassword().isBlank()
                        ? passwordEncoder.encode(request.getPassword())
                        : null)
                .isActive(true)
                .build();

        ShareLink saved = shareLinkRepository.save(link);
        return toResponse(saved);
    }

    public List<ShareLinkResponse> getLinksByFile(Long fileId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return shareLinkRepository.findByFileIdAndIsActiveTrue(fileId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeLink(String token, String username) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link not found"));

        if (!link.getCreatedBy().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        link.setIsActive(false);
        shareLinkRepository.save(link);
    }

    /**
     * Validates a token and returns the associated file metadata.
     * Called by the public download endpoint.
     */
    @Transactional
    public FileMetadata resolveToken(String token, String password) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link not found or has been revoked"));

        if (!link.getIsActive()) {
            throw new RuntimeException("This link has been revoked");
        }

        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now())) {
            link.setIsActive(false);
            shareLinkRepository.save(link);
            throw new RuntimeException("This link has expired");
        }

        if (link.getDownloadLimit() != null && link.getDownloadCount() >= link.getDownloadLimit()) {
            throw new RuntimeException("Download limit reached for this link");
        }

        if (link.getPasswordHash() != null) {
            if (password == null || !passwordEncoder.matches(password, link.getPasswordHash())) {
                throw new RuntimeException("Invalid password");
            }
        }

        // Increment download count
        link.setDownloadCount(link.getDownloadCount() + 1);
        shareLinkRepository.save(link);

        return link.getFile();
    }

    private ShareLinkResponse toResponse(ShareLink link) {
        return ShareLinkResponse.builder()
                .id(link.getId())
                .token(link.getToken())
                // M3: Use configured frontend URL
                .publicUrl(frontendUrl + "/share/" + link.getToken())
                .fileId(link.getFile().getId())
                .fileName(link.getFile().getOriginalName())
                .expiresAt(link.getExpiresAt())
                .downloadLimit(link.getDownloadLimit())
                .downloadCount(link.getDownloadCount())
                .passwordProtected(link.getPasswordHash() != null)
                .active(link.getIsActive())
                .createdAt(link.getCreatedAt())
                .build();
    }
}
