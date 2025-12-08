package com.pluxity.onboarding

import com.pluxity.authentication.security.JwtProvider
import com.pluxity.global.exception.CustomException
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = ["jwt.access-token.expiration=1"])
@Transactional
class JwtExpirationTest
    @Autowired
    constructor(
        private val jwtProvider: JwtProvider,
    ) {
        @Test
        @DisplayName("실패: 만료된 AccessToken 검증 시 CustomException이 발생한다")
        fun isAccessTokenValid_ExpiredToken_ThrowsCustomException() {
            // given - 유효시간 1초인 토큰 생성
            val token = jwtProvider.generateAccessToken("testUsername")

            // when - 2초 대기하여 토큰 만료
            Thread.sleep(2000)

            // then - 만료된 토큰 검증 시 예외 발생
            assertThatThrownBy {
                jwtProvider.isAccessTokenValid(token)
            }.isInstanceOf(CustomException::class.java)
        }
    }
