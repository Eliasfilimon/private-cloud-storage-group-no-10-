package com.udom.securecloud.dto;

import lombok.Data;

@Data
public class CreateShareLinkRequest {
    private Long fileId;
    private Integer expiresInDays;
    private Integer downloadLimit;
    private String password;
}
