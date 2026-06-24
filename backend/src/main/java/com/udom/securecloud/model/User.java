package com.udom.securecloud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "full_name")
    private String fullName; // Computed field for backwards compatibility

    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.STAFF;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Long storageUsed = 0L;

    @Column(name = "storage_quota")
    private Long storageQuota; // Computed from role, not stored

    /**
     * Called ONLY on first INSERT. Sets default quota based on role if none was
     * explicitly provided, and computes fullName.
     */
    @PrePersist
    public void prePersist() {
        this.fullName = this.firstName + " " + this.lastName;
        // Set default quota only if not already assigned (allows overrides at creation time)
        if (this.storageQuota == null || this.storageQuota == 0L) {
            this.storageQuota = getDefaultStorageQuotaByRole();
        }
    }

    /**
     * Called on every UPDATE. Recomputes fullName only.
     * DOES NOT touch storageQuota — custom quotas set by admin must be preserved.
     */
    @PreUpdate
    public void preUpdate() {
        this.fullName = this.firstName + " " + this.lastName;
    }

    /** Returns the default role-based quota. Used only during initial user creation. */
    public Long getDefaultStorageQuotaByRole() {
        return role == Role.ADMIN ? 10737418240L : 5368709120L; // 10 GB / 5 GB
    }

    /**
     * Kept for backward compatibility. Returns the current storageQuota if set,
     * otherwise falls back to the role-based default.
     */
    public Long getStorageQuotaByRole() {
        return this.storageQuota != null ? this.storageQuota : getDefaultStorageQuotaByRole();
    }

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    @Column(name = "totp_secret")
    private String totpSecret;

    @Column(name = "totp_enabled", nullable = false)
    private Boolean totpEnabled = false;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    public enum Role {
        ADMIN,
        STAFF
    }
}
