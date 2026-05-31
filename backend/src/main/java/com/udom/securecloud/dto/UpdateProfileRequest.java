package com.udom.securecloud.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    private String firstName;
    
    private String lastName;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String department;
}
