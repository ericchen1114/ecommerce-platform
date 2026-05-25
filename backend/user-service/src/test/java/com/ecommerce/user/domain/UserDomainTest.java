package com.ecommerce.user.domain;

import com.ecommerce.user.domain.model.User;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserDomainTest {

    @Test
    void 建立用戶_應設定預設角色為USER() {
        User user = User.create("eric", "eric@test.com", "encoded", "Eric", "0912345678");
        assertThat(user.getRole()).isEqualTo(User.UserRole.USER);
        assertThat(user.getUsername()).isEqualTo("eric");
        assertThat(user.getCreatedAt()).isNotNull();
    }
}
