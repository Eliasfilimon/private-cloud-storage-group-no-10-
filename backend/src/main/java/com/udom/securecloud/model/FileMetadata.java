package com.udom.securecloud.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, unique = true)
    private String filePath;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String originalName;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(nullable = false)
    private Boolean isEncrypted = false;

    /**
     * Wrapped encryption key (wrapped with master key before storage).
     * Format: Base64(version + IV + encrypted_key + auth_tag)
     * This is NOT the raw file key, but a wrapped version.
     */
    @Column(length = 500)
    private String wrappedEncryptionKey;

    /**
     * GCM authentication tag stored separately for integrity verification.
     * Not strictly needed since GCM appends it, but useful for explicit verification.
     */
    @Column(length = 50)
    private String authenticationTag;

    /**
     * Version of the master key used for wrapping.
     * Enables key rotation: v1, v2, v3 etc.
     * Default: 1 (initial version)
     */
    @Column(nullable = false)
    private Integer masterKeyVersion = 1;

    /**
     * Legacy checksum (kept for backward compatibility, but not used for new encryptions).
     * GCM authentication tag now handles tamper detection.
     */
    @Column
    private String checksum;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
