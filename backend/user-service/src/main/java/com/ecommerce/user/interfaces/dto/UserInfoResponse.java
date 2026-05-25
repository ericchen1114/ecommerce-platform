package com.ecommerce.user.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserInfoResponse {
    private String        memberId;     // Snowflake ID（字串，避免 JS 精度遺失）
    private String        fullName;
    private String        phone;
    private String        email;
    private LocalDate     birthday;
    private String        address;
    private String        role;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
