package com.pluxity.authentication.security

import com.pluxity.user.entity.User
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CustomUserDetailsTest {
    @Mock
    private lateinit var user: User

    @Test
    @DisplayName("사용자의 비밀번호를 가져올 수 있다")
    fun getPassword() {
        val password = "password123"
        BDDMockito.given(user.password).willReturn(password)
        val userDetails = CustomUserDetails(user)
        val result = userDetails.password
        Assertions.assertThat(result).isEqualTo(password)
    }

    @Test
    @DisplayName("사용자의 아이디를 가져올 수 있다")
    fun getUsername() {
        val username = "testUser"
        BDDMockito.given(user.username).willReturn(username)
        val userDetails = CustomUserDetails(user)
        val result = userDetails.username
        Assertions.assertThat(result).isEqualTo(username)
    }
}
