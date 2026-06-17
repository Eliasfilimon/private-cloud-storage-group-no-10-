package com.udom.securecloud.service;

import com.udom.securecloud.dto.FileResponse;
import com.udom.securecloud.dto.FileUploadResponse;
import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.Folder;
import com.udom.securecloud.model.User;
import com.udom.securecloud.model.SharedFile;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.FolderRepository;
import com.udom.securecloud.repository.SharedFileRepository;
import com.udom.securecloud.repository.UserRepository;
import java.util.Optional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${minio.bucket.name}")
    private String bucketName;

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final SharedFileRepository sharedFileRepository;
    private final FileEncryptionService encryptionService;
    private final AuditLogService auditLogService;
    private final ChecksumService checksumService;
    @Lazy
    private final FileVersionService fileVersionService;

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket", e);
        }
    }

    public FileUploadResponse uploadFile(MultipartFile file, String username, 
                                        Boolean encrypt, Long folderId, String ipAddress, String userAgent) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check storage quota
            long fileSize = file.getSize();
            if (user.getStorageUsed() + fileSize > user.getStorageQuota()) {
                auditLogService.logAction(
                        user.getId(), username, "FILE_UPLOAD", "FILE", null,
                        ipAddress, userAgent, "FAILED",
                        "Storage quota exceeded for file: " + file.getOriginalFilename()
                );
                throw new RuntimeException("Storage quota exceeded");
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            String objectName = user.getUsername() + "/" + uniqueFilename;

            // Read file data
            byte[] fileData = file.getBytes();
            
            // ENCRYPTION OUTSIDE TRANSACTION - CPU intensive operation
            SecretKey fileKey = encryptionService.generateKey();
            String wrappedKey = encryptionService.wrapKey(fileKey);
            int masterKeyVersion = encryptionService.getCurrentKeyVersion();
            byte[] encryptedData = encryptionService.encrypt(fileData, fileKey);
            
            // Log encryption event for audit trail
            auditLogService.logAction(
                    user.getId(),
                    username,
                    "FILE_ENCRYPTION",
                    "ENCRYPTION",
                    null,
                    ipAddress,
                    userAgent,
                    "SUCCESS",
                    "File encrypted with AES-256-GCM before upload to storage: " + originalFilename
            );

            // Calculate checksum for data integrity
            String checksum = checksumService.calculateSHA256(encryptedData);

            // MINIO UPLOAD OUTSIDE TRANSACTION - I/O intensive operation
            try (InputStream bais = new ByteArrayInputStream(encryptedData)) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(bais, encryptedData.length, -1)
                        .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                        .build()
                );
            }

            // SAVE METADATA IN SEPARATE TRANSACTION
            return saveFileMetadata(user, originalFilename, uniqueFilename, objectName, fileSize, 
                                   file.getContentType(), wrappedKey, masterKeyVersion, checksum, 
                                   ipAddress, username, userAgent);

        } catch (Exception e) {
            // Log failed upload
            try {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    auditLogService.logAction(
                            user.getId(), username, "FILE_UPLOAD", "FILE", null,
                            ipAddress, userAgent, "FAILED",
                            "Upload failed: " + e.getMessage()
                    );
                }
            } catch (Exception auditEx) {
                // Audit logging failed, continue with error
            }
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Transactional
    protected FileUploadResponse saveFileMetadata(User user, String originalFilename, String uniqueFilename,
                                               String objectName, long fileSize, String mimeType,
                                               String wrappedKey, int masterKeyVersion, String checksum,
                                               String ipAddress, String username, String userAgent) {
        // Save metadata (no raw file keys stored in DB)
        FileMetadata metadata = new FileMetadata();
        metadata.setUser(user);
        metadata.setFileName(uniqueFilename);
        metadata.setOriginalName(originalFilename);
        metadata.setFilePath(objectName);
        metadata.setFileSize(fileSize);
        metadata.setMimeType(mimeType);
        metadata.setIsEncrypted(true); // Always encrypted
        metadata.setWrappedEncryptionKey(wrappedKey); // Store wrapped key, not raw key
        metadata.setMasterKeyVersion(masterKeyVersion);
        metadata.setChecksum(checksum); // Store checksum for integrity verification
        metadata.setVersion(1);
        metadata.setIsDeleted(false);
        metadata.setCreatedAt(LocalDateTime.now());
        metadata.setUpdatedAt(LocalDateTime.now());

        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

        // Update user storage
        user.setStorageUsed(user.getStorageUsed() + fileSize);
        userRepository.save(user);

        // Log successful upload
        auditLogService.logAction(
                user.getId(),
                username,
                "FILE_UPLOAD",
                "FILE",
                savedMetadata.getId(),
                ipAddress,
                userAgent,
                "SUCCESS",
                "File uploaded and encrypted: " + originalFilename + " (Size: " + fileSize + " bytes, Encryption: AES-256-GCM)"
        );

        return FileUploadResponse.builder()
                .fileId(savedMetadata.getId())
                .fileName(uniqueFilename)
                .originalName(originalFilename)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .encrypted(true)
                .message("File uploaded successfully")
                .build();
    }

    @Transactional
    public List<FileResponse> getUserFiles(String username, Long folderId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FileMetadata> files;
        if (folderId != null) {
            files = fileMetadataRepository
                    .findByUserAndFolderIdAndIsDeletedFalseOrderByCreatedAtDesc(user, folderId);
        } else {
            files = fileMetadataRepository
                    .findByUserAndFolderIsNullAndIsDeletedFalseOrderByCreatedAtDesc(user);
        }

        return files.stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public Resource downloadFile(Long fileId, String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Check if user owns the file or has DOWNLOAD/EDIT permission via share
        if (!metadata.getUser().getId().equals(user.getId())) {
            Optional<SharedFile> share = sharedFileRepository
                    .findByFileIdAndSharedWithIdAndIsActiveTrue(fileId, user.getId());
            boolean canDownload = share.isPresent() &&
                    (share.get().getPermission() == SharedFile.Permission.DOWNLOAD
                            || share.get().getPermission() == SharedFile.Permission.EDIT);
            if (!canDownload) {
                auditLogService.logAction(
                        user.getId(), username, "FILE_DOWNLOAD", "FILE", fileId,
                        ipAddress, userAgent, "FAILED",
                        "Unauthorized access attempt to file: " + metadata.getOriginalName()
                );
                throw new RuntimeException("Unauthorized access to file");
            }
        }

        try {
            String objectName = metadata.getFilePath();
            byte[] fileData;
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
                fileData = stream.readAllBytes();
            }

            // Verify checksum for data integrity
            if (metadata.getChecksum() != null && !metadata.getChecksum().isEmpty()) {
                String actualChecksum = checksumService.calculateSHA256(fileData);
                if (!actualChecksum.equals(metadata.getChecksum())) {
                    auditLogService.logAction(
                            user.getId(),
                            username,
                            "FILE_DOWNLOAD",
                            "FILE",
                            fileId,
                            ipAddress,
                            userAgent,
                            "FAILED",
                            "Data integrity check failed - checksum mismatch for file: " + metadata.getOriginalName()
                    );
                    throw new RuntimeException("File integrity check failed - data may be corrupted");
                }
            }

            // Decrypt if encrypted
            if (metadata.getIsEncrypted() && metadata.getWrappedEncryptionKey() != null) {
                try {
                    // Unwrap the file key using master key
                    SecretKey fileKey = encryptionService.unwrapKey(metadata.getWrappedEncryptionKey());
                    
                    // Decrypt using AES-256-GCM
                    // GCM automatically verifies authentication tag - will throw if tampered
                    fileData = encryptionService.decrypt(fileData, fileKey);
                    
                    // Log successful decryption
                    auditLogService.logAction(
                            user.getId(),
                            username,
                            "FILE_DECRYPT",
                            "FILE",
                            fileId,
                            ipAddress,
                            userAgent,
                            "SUCCESS",
                            "File decrypted for download: " + metadata.getOriginalName()
                    );
                } catch (Exception decryptEx) {
                    // Authentication failure - data tampering detected
                    auditLogService.logAction(
                            user.getId(),
                            username,
                            "FILE_DECRYPT",
                            "FILE",
                            fileId,
                            ipAddress,
                            userAgent,
                            "FAILED",
                            "Authentication failure - possible tampering detected: " + decryptEx.getMessage()
                    );
                    throw new RuntimeException("File decryption failed - data integrity check failed (possible tampering)", decryptEx);
                }
            }

            // H3: Return decrypted bytes directly — no temp file written to disk
            Resource resource = new ByteArrayResource(fileData) {
                @Override
                public String getFilename() {
                    return metadata.getOriginalName();
                }
            };

            // Log successful download
            auditLogService.logAction(
                    user.getId(), username, "FILE_DOWNLOAD", "FILE", fileId,
                    ipAddress, userAgent, "SUCCESS",
                    "File downloaded: " + metadata.getOriginalName() + " (Size: " + metadata.getFileSize() + " bytes)"
            );

            return resource;
        } catch (Exception e) {
            // Log failed download
            auditLogService.logAction(
                    user.getId(),
                    username,
                    "FILE_DOWNLOAD",
                    "FILE",
                    fileId,
                    ipAddress,
                    userAgent,
                    "FAILED",
                    "Download failed: " + e.getMessage()
            );
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    /**
     * C3: Public download via share link — does NOT run as the file owner.
     * Audit log records the share token as the accessor, not the owner's identity.
     */
    @Transactional(readOnly = true)
    public Resource downloadFilePublic(Long fileId, String ipAddress, String userAgent, String shareToken) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(metadata.getFilePath()).build());
            byte[] encryptedData = stream.readAllBytes();

            SecretKey fileKey = encryptionService.unwrapKey(metadata.getWrappedEncryptionKey());
            byte[] fileData   = encryptionService.decrypt(encryptedData, fileKey);

            Resource resource = new ByteArrayResource(fileData) {
                @Override
                public String getFilename() { return metadata.getOriginalName(); }
            };

            // Audit with share token as accessor identity (not file owner)
            auditLogService.logAction(
                    null, "SHARE_LINK:" + shareToken, "FILE_DOWNLOAD_PUBLIC", "FILE",
                    fileId, ipAddress, userAgent, "SUCCESS",
                    "Public share download: " + metadata.getOriginalName()
            );

            return resource;
        } catch (Exception e) {
            auditLogService.logAction(
                    null, "SHARE_LINK:" + shareToken, "FILE_DOWNLOAD_PUBLIC", "FILE",
                    fileId, ipAddress, userAgent, "FAILED",
                    "Public share download failed: " + e.getMessage()
            );
            throw new RuntimeException("Failed to download file via share link: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteFile(Long fileId, String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!metadata.getUser().getId().equals(user.getId())) {
            // Log unauthorized delete attempt
            auditLogService.logAction(
                    user.getId(),
                    username,
                    "FILE_DELETE",
                    "FILE",
                    fileId,
                    ipAddress,
                    userAgent,
                    "FAILED",
                    "Unauthorized delete attempt: " + metadata.getOriginalName()
            );
            throw new RuntimeException("Unauthorized access to file");
        }

        // Soft delete
        metadata.setIsDeleted(true);
        metadata.setUpdatedAt(LocalDateTime.now());
        fileMetadataRepository.save(metadata);

        // Update user storage
        user.setStorageUsed(user.getStorageUsed() - metadata.getFileSize());
        userRepository.save(user);

        // Log successful delete
        auditLogService.logAction(
                user.getId(),
                username,
                "FILE_DELETE",
                "FILE",
                fileId,
                ipAddress,
                userAgent,
                "SUCCESS",
                "File deleted: " + metadata.getOriginalName() + " (Size: " + metadata.getFileSize() + " bytes)"
        );
    }

    @Transactional
    public FileResponse renameFile(Long fileId, String username, String newName) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if (!metadata.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to file");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException("File name cannot be empty");
        }
        metadata.setOriginalName(newName.trim());
        fileMetadataRepository.save(metadata);
        return mapToFileResponse(metadata);
    }

    @Transactional
    public FileResponse moveFile(Long fileId, String username, Long targetFolderId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if (!metadata.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to file");
        }

        Folder targetFolder = null;
        if (targetFolderId != null) {
            targetFolder = folderRepository.findById(targetFolderId)
                    .orElseThrow(() -> new RuntimeException("Target folder not found"));
            if (!targetFolder.getUser().getUsername().equals(username)) {
                throw new RuntimeException("Unauthorized access to target folder");
            }
        }

        metadata.setFolder(targetFolder);
        fileMetadataRepository.save(metadata);
        return mapToFileResponse(metadata);
    }

    private FileResponse mapToFileResponse(FileMetadata metadata) {
        return FileResponse.builder()
                .id(metadata.getId())
                .fileName(metadata.getFileName())
                .originalName(metadata.getOriginalName())
                .fileSize(metadata.getFileSize())
                .mimeType(metadata.getMimeType())
                .encrypted(metadata.getIsEncrypted())
                .version(metadata.getVersion())
                .createdAt(metadata.getCreatedAt())
                .updatedAt(metadata.getUpdatedAt())
                .ownerUsername(metadata.getUser() != null ? metadata.getUser().getUsername() : null)
                .build();
    }

    // ─── Trash operations ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FileResponse> getTrashedFiles(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return fileMetadataRepository.findByUserIdAndIsDeletedTrueOrderByUpdatedAtDesc(user.getId())
                .stream().map(this::mapToFileResponse).collect(Collectors.toList());
    }

    @Transactional
    public void restoreFile(Long fileId, String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if (!metadata.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to file");
        }
        if (!Boolean.TRUE.equals(metadata.getIsDeleted())) {
            throw new RuntimeException("File is not in trash");
        }
        // G6: Check if restoring would exceed current quota
        if (user.getStorageUsed() + metadata.getFileSize() > user.getStorageQuota()) {
            throw new RuntimeException(
                "Cannot restore file: storage quota would be exceeded. " +
                "Free up space or request a quota increase first."
            );
        }
        metadata.setIsDeleted(false);
        metadata.setUpdatedAt(LocalDateTime.now());
        fileMetadataRepository.save(metadata);
        user.setStorageUsed(user.getStorageUsed() + metadata.getFileSize());
        userRepository.save(user);
        auditLogService.logAction(user.getId(), username, "FILE_RESTORE", "FILE", fileId,
                ipAddress, userAgent, "SUCCESS", "File restored: " + metadata.getOriginalName());
    }

    @Transactional
    public void permanentDeleteFile(Long fileId, String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if (!metadata.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to file");
        }
        // Remove from object storage
        try {
            minioClient.removeObject(io.minio.RemoveObjectArgs.builder()
                    .bucket(bucketName).object(metadata.getFilePath()).build());
        } catch (Exception e) {
            // Log but don't fail — file may already be gone
        }
        fileMetadataRepository.delete(metadata);
        auditLogService.logAction(user.getId(), username, "FILE_PERMANENT_DELETE", "FILE", fileId,
                ipAddress, userAgent, "SUCCESS", "File permanently deleted: " + metadata.getOriginalName());
    }

    @Transactional
    public void emptyTrash(String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<FileMetadata> trashed = fileMetadataRepository.findByUserIdAndIsDeletedTrueOrderByUpdatedAtDesc(user.getId());
        for (FileMetadata m : trashed) {
            try {
                minioClient.removeObject(io.minio.RemoveObjectArgs.builder()
                        .bucket(bucketName).object(m.getFilePath()).build());
            } catch (Exception e) { /* ignore */ }
        }
        fileMetadataRepository.deleteAll(trashed);
        auditLogService.logAction(user.getId(), username, "TRASH_EMPTY", "FILE", null,
                ipAddress, userAgent, "SUCCESS", "Emptied trash (" + trashed.size() + " files)");
    }

    // ─── Gap 3: Search ────────────────────────────────────────────────────────

    public List<FileResponse> searchFiles(String username, String query) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return fileMetadataRepository.searchByUser(user, query).stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    // ─── Gap 5: In-browser Preview ────────────────────────────────────────────

    @Data
    public static class PreviewResult {
        private final Resource resource;
        private final String mimeType;
        private final String originalName;
    }

    @Transactional(readOnly = true)
    public PreviewResult previewFile(Long fileId, String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Owner or any active share (VIEW/DOWNLOAD/EDIT) can preview
        if (!metadata.getUser().getId().equals(user.getId())) {
            Optional<SharedFile> share = sharedFileRepository
                    .findByFileIdAndSharedWithIdAndIsActiveTrue(fileId, user.getId());
            if (share.isEmpty()) {
                throw new RuntimeException("Unauthorized access to file");
            }
        }

        try {
            String objectName = metadata.getFilePath();
            byte[] fileData;
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {
                fileData = stream.readAllBytes();
            }

            if (metadata.getIsEncrypted() && metadata.getWrappedEncryptionKey() != null) {
                SecretKey fileKey = encryptionService.unwrapKey(metadata.getWrappedEncryptionKey());
                fileData = encryptionService.decrypt(fileData, fileKey);
            }

            String mimeType = metadata.getMimeType() != null ? metadata.getMimeType() : "application/octet-stream";
            Resource resource = new ByteArrayResource(fileData);
            return new PreviewResult(resource, mimeType, metadata.getOriginalName());

        } catch (Exception e) {
            throw new RuntimeException("Failed to prepare preview: " + e.getMessage(), e);
        }
    }
}
