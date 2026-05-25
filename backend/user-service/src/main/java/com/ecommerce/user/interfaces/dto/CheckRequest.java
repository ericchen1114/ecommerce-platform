package com.ecommerce.user.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 檢查識別符（手機號或 Email）是否已是會員
 */
@Data
public class CheckRequest {

    @NotBlank(message = "識別符不可為空")
    private String identifier;
}
