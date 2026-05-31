package com.udom.securecloud.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String backupType; // FULL, DATABASE_ONLY

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED

    private String message;

    private Integer userCount;

    private Integer fileCount;

    private Integer auditLogCount;

    @Column(nullable = false)
    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
