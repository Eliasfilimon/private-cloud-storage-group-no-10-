package com.udom.securecloud.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveStorageRequest {
    
    @NotNull(message = "Approval decision is required")
    private Boolean approved;
    
    @Size(max = 1000, message = "Admin notes must be less than 1000 characters")
    private String adminNotes;
    
    // Optional: Override the requested quota (in GB)
    private Integer approvedQuotaGb;
}
