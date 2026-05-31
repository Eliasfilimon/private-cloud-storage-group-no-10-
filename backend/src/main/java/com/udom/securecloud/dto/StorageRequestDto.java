package com.udom.securecloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageRequestDto {
    private Long id;
    
    private Long userId;
    private String userEmail;
    private String userFullName;
    private String userRole;
    
    @NotNull(message = "Requested quota is required")
    private Integer requestedQuotaGb;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason must be less than 1000 characters")
    private String reason;
    
    private String adminNotes;
    private String status;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private Integer previousQuotaGb;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
