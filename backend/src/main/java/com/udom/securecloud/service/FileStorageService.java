package com.udom.securecloud.service;

import com.udom.securecloud.dto.FileResponse;
import com.udom.securecloud.dto.FileUploadResponse;
import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final FileEncryptionService encryptionService;
    private final AuditLogService auditLogService;

    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, String username, 
                                        Boolean encrypt, String ipAddress, String userAgent) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check storage quota
            long fileSize = file.getSize();
            if (user.getStorageUsed() + fileSize > user.getStorageQuota()) {
                throw new RuntimeException("Storage quota exceeded");
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, user.getUsername());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Read file data
            byte[] fileData = file.getBytes();
            String checksum = encryptionService.calculateChecksum(fileData);

            // Encrypt if requested
            String encryptionKey = null;
            if (encrypt != null && encrypt) {
                SecretKey key = encryptionService.generateKey();
                encryptionKey = encryptionService.encodeKey(key);
                fileData = encryptionService.encrypt(fileData, key);
            }

            // Save file to disk
            Files.write(filePath, fileData);

            // Save metadata
            FileMetadata metadata = new FileMetadata();
            metadata.setUserId(user.getId());
            metadata.setFileName(uniqueFilename);
            metadata.setOriginalName(originalFilename);
            metadata.setFilePath(filePath.toString());
            metadata.setFileSize(fileSize);
            metadata.setMimeType(file.getContentType());
            metadata.setIsEncrypted(encrypt != null && encrypt);
            metadata.setEncryptionKey(encryptionKey);
            metadata.setChecksum(checksum);
            metadata.setVersion(1);
            metadata.setIsDeleted(false);
            metadata.setCreatedAt(LocalDateTime.now());
            metadata.setUpdatedAt(LocalDateTime.now());

            FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

            // Update user storage
            user.setStorageUsed(user.getStorageUsed() + fileSize);
            userRepository.save(user);

            // Log the action
            auditLogService.logAction(
                    user.getId(),
                    username,
                    "FILE_UPLOAD",
                    "FILE",
                    savedMetadata.getId(),
                    ipAddress,
                    userAgent,
                    "SUCCESS",
                    "File uploaded: " + originalFilename
            );

            return FileUploadResponse.builder()
                    .fileId(savedMetadata.getId())
                    .fileName(uniqueFilename)
                    .originalName(originalFilename)
                    .fileSize(fileSize)
                    .mimeType(file.getContentType())
                    .encrypted(encrypt != null && encrypt)
                    .message("File uploaded successfully")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getUserFiles(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FileMetadata> files = fileMetadataRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId());

        return files.stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId, String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Check if user owns the file
        if (!metadata.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to file");
        }

        try {
            Path filePath = Paths.get(metadata.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Decrypt if needed
                if (metadata.getIsEncrypted() && metadata.getEncryptionKey() != null) {
                    byte[] encryptedData = Files.readAllBytes(filePath);
                    SecretKey key = encryptionService.decodeKey(metadata.getEncryptionKey());
                    byte[] decryptedData = encryptionService.decrypt(encryptedData, key);
                    
                    // Create temporary file for decrypted content
                    Path tempFile = Files.createTempFile("download-", metadata.getOriginalName());
                    Files.write(tempFile, decryptedData);
                    resource = new UrlResource(tempFile.toUri());
                }

                // Log the action
                auditLogService.logAction(
                        user.getId(),
                        username,
                        "FILE_DOWNLOAD",
                        "FILE",
                        fileId,
                        ipAddress,
                        userAgent,
                        "SUCCESS",
                        "File downloaded: " + metadata.getOriginalName()
                );

                return resource;
            } else {
                throw new RuntimeException("File not found or not readable");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteFile(Long fileId, String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!metadata.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to file");
        }

        // Soft delete
        metadata.setIsDeleted(true);
        metadata.setUpdatedAt(LocalDateTime.now());
        fileMetadataRepository.save(metadata);

        // Update user storage
        user.setStorageUsed(user.getStorageUsed() - metadata.getFileSize());
        userRepository.save(user);

        // Log the action
        auditLogService.logAction(
                user.getId(),
                username,
                "FILE_DELETE",
                "FILE",
                fileId,
                ipAddress,
                userAgent,
                "SUCCESS",
                "File deleted: " + metadata.getOriginalName()
        );
    }

    private FileResponse mapToFileResponse(FileMetadata metadata) {
        User user = userRepository.findById(metadata.getUserId()).orElse(null);
        
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
                .ownerUsername(user != null ? user.getUsername() : null)
                .build();
    }
}
