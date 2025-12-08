package com.pluxity.onboarding

import com.pluxity.authentication.dto.SignInRequest
import com.pluxity.authentication.security.JwtProvider
import com.pluxity.authentication.service.AuthenticationService
import com.pluxity.user.entity.User
import com.pluxity.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootTest
@Transactional
class JwtProviderTest
    @Autowired
    constructor(
        private val jwtProvider: JwtProvider,
        private val authenticationService: AuthenticationService,
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
    ) {
        private var user: User? = null

        @BeforeEach
        fun setUp() {
            user =
                userRepository.save(
                    User(
                        username = "testUsername",
                        password = passwordEncoder.encode("password"),
                        name = "name",
                        code = "code",
                    ),
                )
        }

        @Test
        @DisplayName("성공: 로그인 시 유효한 AccessToken이 생성되고 쿠키에 담긴다")
        fun signIn_ValidCredentials_GeneratesAccessTokenInCookie() {
            // given
            val signInRequest = SignInRequest("testUsername", "password")
            val request = MockHttpServletRequest()
            val response = MockHttpServletResponse()

            // when
            authenticationService.signIn(signInRequest, request, response)

            // then
            val accessTokenCookie = response.getCookie("AccessToken")
            assertThat(accessTokenCookie).isNotNull
            val accessToken = accessTokenCookie!!.value

            val extractUsername = jwtProvider.extractUsername(accessToken)
            assertThat(extractUsername).isEqualTo(signInRequest.username)
        }

        @Test
        @DisplayName("성공: AccessToken 생성 시 정확한 username이 포함된다")
        fun generateAccessToken_ReturnsTokenWithCorrectUsername() {
            // given
            val username = "testUsername"

            // when
            val token = jwtProvider.generateAccessToken(username)

            // then
            val extractedUsername = jwtProvider.extractUsername(token)
            assertThat(extractedUsername).isEqualTo(username)
        }

        @Test
        @DisplayName("성공: 유효한 AccessToken 검증 시 true를 반환한다")
        fun isAccessTokenValid_ValidToken_ReturnsTrue() {
            // given
            val token = jwtProvider.generateAccessToken("testUsername")

            // when
            val isValid = jwtProvider.isAccessTokenValid(token)

            // then
            assertThat(isValid).isTrue()
        }
    }
