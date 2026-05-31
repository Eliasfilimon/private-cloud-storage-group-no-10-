package com.udom.securecloud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "storage_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "requested_quota_gb", nullable = false)
    private Integer requestedQuotaGb; // Requested total quota in GB

    @NotBlank
    @Column(name = "reason", nullable = false, length = 1000)
    private String reason; // Why they need more storage

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes; // Admin's notes on approval/rejection

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "previous_quota_gb")
    private Integer previousQuotaGb; // Store previous quota for audit

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}
