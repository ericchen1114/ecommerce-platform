package com.ecommerce.user.interfaces.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String        token;
    private UserInfoResponse user;
}
