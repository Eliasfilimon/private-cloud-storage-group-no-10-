package com.udom.securecloud.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "share_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileMetadata file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    /** Optional expiry; null = never expires */
    private LocalDateTime expiresAt;

    /** Optional download cap; null = unlimited */
    private Integer downloadLimit;

    @Column(nullable = false)
    private Integer downloadCount = 0;

    /** BCrypt-hashed password; null = no password required */
    @Column(length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
