package com.ecommerce.user.infrastructure.persistence.jpa;

import com.ecommerce.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 會員 JPA Entity（Infrastructure Layer）
 *
 * <p>對應資料庫 {@code members} 表，負責 ORM 映射。
 * 不包含任何業務邏輯，轉換邏輯由
 * {@link com.ecommerce.user.infrastructure.persistence.mapper.UserEntityMapper} 處理（MapStruct 自動生成）。</p>
 *
 * <h3>欄位說明</h3>
 * <ul>
 *   <li>{@code id}       — DB 內部主鍵，AUTO_INCREMENT，不對外暴露</li>
 *   <li>{@code memberNo} — 對外顯示的會員編號（M + 8 位隨機數字），UNIQUE</li>
 *   <li>{@code phone}    — 手機號，主要登入識別符，UNIQUE</li>
 *   <li>{@code email}    — Email，選填，UNIQUE</li>
 * </ul>
 */
@Data
@Entity
@Table(name = "members", indexes = {
    @Index(name = "idx_members_member_no", columnList = "memberNo", unique = true),
    @Index(name = "idx_members_phone",     columnList = "phone",    unique = true),
    @Index(name = "idx_members_email",     columnList = "email",    unique = true)
})
public class UserJpaEntity {

    /** DB 內部主鍵，AUTO_INCREMENT，不對外暴露 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 對外顯示的會員編號，格式 M + 8 位隨機數字，例如 M83729471 */
    @Column(name = "member_no", unique = true, nullable = false, length = 10)
    private String memberNo;

    /** Email，選填，唯一 */
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /** 手機號，主要登入識別符，唯一 */
    @Column(unique = true, nullable = false, length = 20)
    private String phone;

    /** BCrypt 加密後的密碼 */
    @Column(nullable = false)
    private String password;

    /** 真實姓名 */
    @Column(nullable = false, length = 50)
    private String fullName;

    /** 出生年月日 */
    @Column
    private LocalDate birthday;

    /** 聯絡地址 */
    @Column(length = 255)
    private String address;

    /** 會員角色（USER / ADMIN） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private User.UserRole role;

    /** 建立時間（不可更新） */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 最後更新時間 */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
