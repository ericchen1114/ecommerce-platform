package com.ecommerce.user.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {

    /** 真實姓名 */
    @NotBlank(message = "姓名不可為空")
    @Size(max = 50, message = "姓名最多 50 字")
    private String fullName;

    /** 手機號（作為登入帳號）*/
    @NotBlank(message = "手機號不可為空")
    @Pattern(regexp = "^09\\d{8}$", message = "手機號格式不正確（09XXXXXXXX）")
    private String phone;

    /** Email（選填，但若填寫需符合格式）*/
    @Email(message = "Email 格式不正確")
    private String email;

    /** 出生年月日 */
    @NotNull(message = "出生年月日不可為空")
    @Past(message = "出生日期必須是過去的日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    /** 地址 */
    @NotBlank(message = "地址不可為空")
    @Size(max = 255, message = "地址最多 255 字")
    private String address;

    /** 密碼 */
    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, max = 64, message = "密碼長度 8–64 字元")
    private String password;
}
