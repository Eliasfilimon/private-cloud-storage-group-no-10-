package com.udom.securecloud.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileMetadata file;

    @Column(nullable = false)
    private Integer versionNumber;

    /** MinIO object path for this specific version */
    @Column(nullable = false, length = 500)
    private String minioObjectPath;

    @Column(nullable = false)
    private Long fileSize;

    /** Wrapped AES key for this version */
    @Column(length = 500)
    private String wrappedEncryptionKey;

    @Column(nullable = false)
    private Integer masterKeyVersion = 1;

    @Column(nullable = false)
    private String createdByUsername;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
