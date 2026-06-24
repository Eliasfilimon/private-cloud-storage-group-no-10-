package com.udom.securecloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Sanitized FileMetadata projection used exclusively for backup exports.
 *
 * Deliberately excludes JPA entity relationships (@ManyToOne User, Folder) to prevent
 * Jackson infinite-recursion / StackOverflowError when serializing the full entity graph.
 * Also excludes wrappedEncryptionKey and authenticationTag for security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupFileMetadataDto {
    private Long          id;
    private String        fileName;
    private String        originalName;
    private String        filePath;
    private String        mimeType;
    private Long          fileSize;
    /** Owner's user ID — no full User object to avoid circular serialization. */
    private Long          userId;
    private String        userEmail;
    /** Folder ID if file is in a folder, null if root. */
    private Long          folderId;
    private Boolean       isEncrypted;
    /** Master key version used to wrap the file key. Used for rotation tracking. */
    private Integer       masterKeyVersion;
    private String        checksum;
    private Integer       version;
    private Boolean       isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
