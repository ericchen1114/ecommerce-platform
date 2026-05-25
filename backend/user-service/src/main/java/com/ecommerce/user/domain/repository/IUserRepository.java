package com.ecommerce.user.domain.repository;

import com.ecommerce.user.domain.model.User;
import java.util.Optional;

/**
 * 會員儲存庫介面（Domain Layer）
 *
 * <p>定義 Domain 層對持久化的操作契約，不依賴任何具體實作（JPA / JDBC / MyBatis）。
 * 實作類位於 {@code infrastructure/persistence/jpa/UserRepositoryImpl}。</p>
 */
public interface IUserRepository {

    /**
     * 儲存或更新會員
     *
     * @param user 要儲存的 {@link User} 物件（id 為 null 時執行 INSERT）
     * @return     儲存後帶有 DB 生成 id 的 {@link User} 物件
     */
    User save(User user);

    /**
     * 以 Email 查詢會員
     *
     * @param email 要查詢的 Email
     * @return      若存在則包裝在 {@link Optional} 內；否則 {@link Optional#empty()}
     */
    Optional<User> findByEmail(String email);

    /**
     * 以手機號查詢會員
     *
     * @param phone 要查詢的手機號（格式：09XXXXXXXX）
     * @return      若存在則包裝在 {@link Optional} 內；否則 {@link Optional#empty()}
     */
    Optional<User> findByPhone(String phone);

    /**
     * 以對外會員編號查詢會員
     *
     * @param memberNo 對外會員編號（格式：M + 8 位數字，例如 M83729471）
     * @return         若存在則包裝在 {@link Optional} 內；否則 {@link Optional#empty()}
     */
    Optional<User> findByMemberNo(String memberNo);

    /**
     * 檢查 Email 是否已被使用
     *
     * @param email 要檢查的 Email
     * @return      {@code true} 表示已存在；{@code false} 表示可使用
     */
    boolean existsByEmail(String email);

    /**
     * 檢查手機號是否已被使用
     *
     * @param phone 要檢查的手機號
     * @return      {@code true} 表示已存在；{@code false} 表示可使用
     */
    boolean existsByPhone(String phone);

    /**
     * 檢查對外會員編號是否已被使用（用於產生時的碰撞偵測）
     *
     * @param memberNo 要檢查的會員編號
     * @return         {@code true} 表示已存在；{@code false} 表示可使用
     */
    boolean existsByMemberNo(String memberNo);
}
