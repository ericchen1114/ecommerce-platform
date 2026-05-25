package com.ecommerce.user.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 會員聚合根（Domain Model）
 *
 * <p>純 Java 物件，不依賴任何框架，代表業務上的「會員」概念。
 * 所有欄位透過 {@link Builder} 建立，建立後不可修改（immutable）。</p>
 *
 * <p>欄位說明：
 * <ul>
 *   <li>{@code id}       - DB 內部主鍵（AUTO_INCREMENT），不對外暴露</li>
 *   <li>{@code memberNo} - 對外顯示的會員編號，格式 M + 8 位隨機數字</li>
 *   <li>{@code phone}    - 手機號，作為主要登入識別符</li>
 *   <li>{@code email}    - Email，選填，可作為登入識別符</li>
 * </ul>
 * </p>
 */
@Getter
@Builder
public class User {

    /** DB 內部主鍵 AUTO_INCREMENT，不對外暴露 */
    private Long          id;

    /** 對外顯示的會員編號，例如 M83729471，直接存 DB 可直查 */
    private String        memberNo;

    /** Email，選填 */
    private String        email;

    /** 手機號，主要登入識別符 */
    private String        phone;

    /** BCrypt 加密後的密碼，明碼絕不存入 */
    private String        password;

    /** 真實姓名 */
    private String        fullName;

    /** 出生年月日 */
    private LocalDate     birthday;

    /** 聯絡地址 */
    private String        address;

    /** 會員角色 */
    private UserRole      role;

    /** 建立時間（不可更新） */
    private LocalDateTime createdAt;

    /** 最後更新時間 */
    private LocalDateTime updatedAt;

    /**
     * 建立一個新會員物件（工廠方法）
     *
     * <p>{@code id} 欄位不傳入，由 JPA 儲存後由 DB AUTO_INCREMENT 自動填入。</p>
     *
     * @param memberNo        對外顯示的會員編號（由 {@code MemberNoGenerator} 產生）
     * @param email           Email，可為 {@code null} 或空字串（選填）
     * @param phone           手機號，不可為空
     * @param encodedPassword 已經過 BCrypt 加密的密碼
     * @param fullName        真實姓名
     * @param birthday        出生年月日
     * @param address         聯絡地址
     * @return 尚未持久化的 {@code User} 物件
     */
    public static User create(String memberNo,
                              String email,
                              String phone,
                              String encodedPassword,
                              String fullName,
                              LocalDate birthday,
                              String address) {
        return User.builder()
                .memberNo(memberNo)
                .email(email)
                .phone(phone)
                .password(encodedPassword)
                .fullName(fullName)
                .birthday(birthday)
                .address(address)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /** 會員角色列舉 */
    public enum UserRole {
        /** 一般會員 */
        USER,
        /** 後台管理員 */
        ADMIN
    }
}
