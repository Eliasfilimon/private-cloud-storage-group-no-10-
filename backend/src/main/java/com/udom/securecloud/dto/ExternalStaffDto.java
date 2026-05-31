package com.udom.securecloud.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalStaffDto {
    private String staffId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String department;
    private String college;
    private String role;
}
