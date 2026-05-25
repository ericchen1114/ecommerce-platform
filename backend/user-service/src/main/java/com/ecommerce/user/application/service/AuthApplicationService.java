package com.ecommerce.user.application.service;

import com.ecommerce.user.application.command.LoginCommand;
import com.ecommerce.user.application.command.RegisterCommand;
import com.ecommerce.user.domain.model.User;
import com.ecommerce.user.domain.repository.IUserRepository;
import com.ecommerce.user.infrastructure.config.JwtProvider;
import com.ecommerce.user.infrastructure.config.MemberNoGenerator;
import com.ecommerce.user.interfaces.dto.AuthResponse;
import com.ecommerce.user.interfaces.mapper.UserDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 會員認證應用服務（Application Service）
 *
 * <p>協調 Domain 層、基礎設施層，完成登入、註冊、帳號查詢等業務流程。</p>
 *
 * <h3>MapStruct 整合</h3>
 * <p>原有 {@code buildAuthResponse()} 手動組裝 {@link com.ecommerce.user.interfaces.dto.UserInfoResponse}
 * 已改由 {@link UserDtoMapper#toUserInfoResponse(User)} 自動轉換：
 * <ul>
 *   <li>{@code memberNo} → {@code memberId}</li>
 *   <li>{@code role.name()} → {@code role}（enum 轉 String）</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final IUserRepository   userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JwtProvider       jwtProvider;
    private final MemberNoGenerator memberNoGenerator;
    private final UserDtoMapper     userDtoMapper;

    /**
     * 檢查識別符是否已是會員
     *
     * @param identifier 手機號（09XXXXXXXX）或 Email
     * @return           {@code true} 表示已是會員
     */
    @Transactional(readOnly = true)
    public boolean checkExists(String identifier) {
        return isPhone(identifier)
               ? userRepository.existsByPhone(identifier)
               : userRepository.existsByEmail(identifier);
    }

    /**
     * 新會員註冊
     *
     * <p>流程：驗證唯一性 → 產生會員編號 → BCrypt 加密 → 儲存 → 簽發 JWT</p>
     *
     * @param cmd 含 fullName、phone、email、birthday、address、password 的 Command
     * @return    含 JWT Token 與會員資訊的 {@link AuthResponse}
     * @throws IllegalArgumentException 手機號或 Email 已存在時拋出
     */
    @Transactional
    public AuthResponse register(RegisterCommand cmd) {
        if (userRepository.existsByPhone(cmd.getPhone()))
            throw new IllegalArgumentException("該手機號已被註冊（code:1002）");
        if (cmd.getEmail() != null && !cmd.getEmail().isBlank()
                && userRepository.existsByEmail(cmd.getEmail()))
            throw new IllegalArgumentException("該 Email 已被使用（code:1003）");

        String memberNo = generateUniqueMemberNo();
        User user = User.create(
                memberNo,
                cmd.getEmail(),
                cmd.getPhone(),
                passwordEncoder.encode(cmd.getPassword()),
                cmd.getFullName(),
                cmd.getBirthday(),
                cmd.getAddress()
        );
        User saved = userRepository.save(user);
        String token = jwtProvider.generate(saved.getMemberNo(), saved.getRole().name());

        // MapStruct：User → UserInfoResponse（自動處理 memberNo→memberId、role enum→String）
        return AuthResponse.builder()
                .token(token)
                .user(userDtoMapper.toUserInfoResponse(saved))
                .build();
    }

    /**
     * 會員登入
     *
     * <p>支援手機號或 Email 登入，帳號不存在與密碼錯誤皆回傳相同訊息（防枚舉攻擊）。</p>
     *
     * @param cmd 含 identifier（手機或 Email）與 password 的 Command
     * @return    含 JWT Token 與會員資訊的 {@link AuthResponse}
     * @throws IllegalArgumentException 帳號不存在或密碼錯誤時拋出
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginCommand cmd) {
        User user = isPhone(cmd.getIdentifier())
                ? userRepository.findByPhone(cmd.getIdentifier())
                        .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤（code:1001）"))
                : userRepository.findByEmail(cmd.getIdentifier())
                        .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤（code:1001）"));

        if (!passwordEncoder.matches(cmd.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("帳號或密碼錯誤（code:1001）");

        String token = jwtProvider.generate(user.getMemberNo(), user.getRole().name());

        // MapStruct：User → UserInfoResponse
        return AuthResponse.builder()
                .token(token)
                .user(userDtoMapper.toUserInfoResponse(user))
                .build();
    }

    /**
     * 產生全局唯一的會員編號（最多重試 5 次）
     *
     * @return 唯一的會員編號字串（例如 M83729471）
     * @throws IllegalStateException 5 次均碰撞時拋出（極罕見）
     */
    private String generateUniqueMemberNo() {
        for (int i = 0; i < 5; i++) {
            String candidate = memberNoGenerator.generate();
            if (!userRepository.existsByMemberNo(candidate)) return candidate;
        }
        throw new IllegalStateException("會員編號產生失敗，請稍後再試");
    }

    /**
     * 判斷識別符是否為手機號格式（09XXXXXXXX）
     *
     * @param identifier 待判斷字串
     * @return           {@code true} 表示符合手機號格式
     */
    private boolean isPhone(String identifier) {
        return identifier != null && identifier.matches("^09\\d{8}$");
    }
}
