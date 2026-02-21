package com.udom.securecloud.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SharedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fileId;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String ownerUsername;

    @Column(nullable = false)
    private Long sharedWithId;

    @Column(nullable = false)
    private String sharedWithUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission = Permission.VIEW;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime sharedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime lastAccessedAt;

    public enum Permission {
        VIEW,
        DOWNLOAD,
        EDIT
    }
}
