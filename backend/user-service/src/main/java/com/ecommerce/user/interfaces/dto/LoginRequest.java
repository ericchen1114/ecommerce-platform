package com.ecommerce.user.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /** 識別符：手機號 或 Email */
    @NotBlank(message = "帳號不可為空")
    private String identifier;

    /** 密碼 */
    @NotBlank(message = "密碼不可為空")
    private String password;
}
