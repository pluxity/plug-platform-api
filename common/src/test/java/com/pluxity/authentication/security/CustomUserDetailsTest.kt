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
internal class CustomUserDetailsTest {
    @Mock
    private val user: User? = null

    @Test
    @DisplayName("사용자의 비밀번호를 가져올 수 있다")
    fun getPassword() {
        // given
        val password = "password123"
        BDDMockito.given<String>(user!!.password).willReturn(password)

        val userDetails = CustomUserDetails(user)

        // when
        val result = userDetails.getPassword()

        // then
        Assertions.assertThat(result).isEqualTo(password)
    }

    @Test
    @DisplayName("사용자의 아이디를 가져올 수 있다")
    fun getUsername() {
        // given
        val username = "testUser"
        BDDMockito.given<String>(user!!.username).willReturn(username)

        val userDetails = CustomUserDetails(user)

        // when
        val result = userDetails.getUsername()

        // then
        Assertions.assertThat(result).isEqualTo(username)
    }
}
