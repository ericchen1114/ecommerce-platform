package com.ecommerce.user.interfaces.mapper;

import com.ecommerce.user.application.command.RegisterCommand;
import com.ecommerce.user.domain.model.User;
import com.ecommerce.user.interfaces.dto.RegisterRequest;
import com.ecommerce.user.interfaces.dto.UserInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 會員 DTO ↔ Domain/Command 轉換器（MapStruct）
 *
 * <p>負責 interfaces 層（DTO）與 application 層（Command）、domain 層（Model）之間的轉換，
 * 取代 {@code AuthController} 和 {@code AuthApplicationService} 中的手動組裝程式碼。</p>
 *
 * <h3>轉換對照</h3>
 * <pre>
 * RegisterRequest  →  RegisterCommand
 *   fullName       →    fullName
 *   phone          →    phone
 *   email          →    email
 *   birthday       →    birthday
 *   address        →    address
 *   password       →    password  （明碼，Service 負責加密）
 *
 * User（domain）   →  UserInfoResponse
 *   memberNo       →    memberId   （欄位名稱不同，需 @Mapping 指定）
 *   fullName       →    fullName
 *   phone          →    phone
 *   email          →    email
 *   birthday       →    birthday
 *   address        →    address
 *   role.name()    →    role       （enum → String，uses expression）
 *   createdAt      →    createdAt
 * </pre>
 */
@Mapper
public interface UserDtoMapper {

    /**
     * RegisterRequest → RegisterCommand
     *
     * <p>全欄位同名對應，無需特殊設定。
     * 密碼由 Service 層加密，此處直接轉移明碼。</p>
     *
     * @param request 前端送來的註冊請求 DTO
     * @return        傳入 Application Service 的 Command 物件
     */
    RegisterCommand toRegisterCommand(RegisterRequest request);

    /**
     * User（domain）→ UserInfoResponse
     *
     * <p>欄位說明：
     * <ul>
     *   <li>{@code memberNo} → {@code memberId}：對外顯示的會員編號，欄位名稱不同</li>
     *   <li>{@code role} → {@code role}：enum 轉 String（{@code role.name()}）</li>
     * </ul>
     * </p>
     *
     * @param user 已持久化的會員 Domain Model
     * @return     API 回應用的使用者資訊 DTO
     */
    @Mapping(target = "memberId", source = "memberNo")
    @Mapping(target = "role",     expression = "java(user.getRole().name())")
    UserInfoResponse toUserInfoResponse(User user);
}
