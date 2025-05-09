package com.pluxity.authentication.security;

import com.pluxity.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsTest {

    @Mock
    private User user;

    @Test
    @DisplayName("사용자의 비밀번호를 가져올 수 있다")
    void getPassword() {
        // given
        String password = "password123";
        given(user.getPassword()).willReturn(password);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // when
        String result = userDetails.getPassword();

        // then
        assertThat(result).isEqualTo(password);
    }

    @Test
    @DisplayName("사용자의 아이디를 가져올 수 있다")
    void getUsername() {
        // given
        String username = "testUser";
        given(user.getUsername()).willReturn(username);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // when
        String result = userDetails.getUsername();

        // then
        assertThat(result).isEqualTo(username);
    }
} 