package com.udom.securecloud.service;

import com.udom.securecloud.dto.FileVersionResponse;
import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.FileVersion;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.FileVersionRepository;
import com.udom.securecloud.repository.UserRepository;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileVersionService {

    private final FileVersionRepository fileVersionRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    /**
     * Snapshot the current state of a file into the version history.
     * Called BEFORE overwriting the file in MinIO.
     */
    @Transactional
    public void snapshotCurrentVersion(FileMetadata file, String performedByUsername) {
        int nextVersionNumber = fileVersionRepository.countByFile(file) + 1;

        FileVersion version = FileVersion.builder()
                .file(file)
                .versionNumber(nextVersionNumber)
                .minioObjectPath(file.getFilePath())
                .fileSize(file.getFileSize())
                .wrappedEncryptionKey(file.getWrappedEncryptionKey())
                .masterKeyVersion(file.getMasterKeyVersion())
                .createdByUsername(performedByUsername)
                .build();

        fileVersionRepository.save(version);
    }

    public List<FileVersionResponse> getVersions(Long fileId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return fileVersionRepository.findByFileOrderByVersionNumberDesc(file).stream()
                .map(v -> FileVersionResponse.builder()
                        .id(v.getId())
                        .fileId(fileId)
                        .versionNumber(v.getVersionNumber())
                        .fileSize(v.getFileSize())
                        .createdByUsername(v.getCreatedByUsername())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void restoreVersion(Long fileId, Long versionId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        FileVersion version = fileVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        if (!version.getFile().getId().equals(fileId)) {
            throw new RuntimeException("Version does not belong to this file");
        }

        try {
            // Snapshot current state before restoring
            snapshotCurrentVersion(file, username);

            // Copy the old version's object to the current file's path
            String restoredObjectPath = version.getMinioObjectPath();
            String currentObjectPath = file.getFilePath();

            // Only copy if paths differ (they should differ by design)
            if (!restoredObjectPath.equals(currentObjectPath)) {
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucketName)
                                .object(currentObjectPath)
                                .source(CopySource.builder()
                                        .bucket(bucketName)
                                        .object(restoredObjectPath)
                                        .build())
                                .build()
                );
            }

            // Update file metadata to reflect restored version
            file.setWrappedEncryptionKey(version.getWrappedEncryptionKey());
            file.setMasterKeyVersion(version.getMasterKeyVersion());
            file.setFileSize(version.getFileSize());
            file.setVersion(file.getVersion() + 1);
            file.setUpdatedAt(LocalDateTime.now());
            fileMetadataRepository.save(file);

        } catch (Exception e) {
            throw new RuntimeException("Failed to restore version: " + e.getMessage(), e);
        }
    }
}
