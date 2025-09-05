package com.pluxity.authentication.service

import com.pluxity.authentication.dto.SignInRequest
import com.pluxity.authentication.dto.SignUpRequest
import com.pluxity.authentication.entity.RefreshToken
import com.pluxity.authentication.repository.RefreshTokenRepository
import com.pluxity.authentication.security.JwtProvider
import com.pluxity.config.MockBeansConfig
import com.pluxity.global.exception.CustomException
import com.pluxity.user.entity.User
import com.pluxity.user.repository.UserRepository
import jakarta.persistence.EntityManager
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
class AuthenticationServiceTest(
    @Autowired private val authenticationService: AuthenticationService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val refreshTokenRepository: RefreshTokenRepository,
    @Autowired private val passwordEncoder: PasswordEncoder,
    @Autowired private val jwtProvider: JwtProvider,
    @Autowired private val em: EntityManager,
    @Value("\${jwt.refresh-token.name}") private val REFRESH_TOKEN_NAME: String,
    @Value("\${jwt.access-token.name}") private val ACCESS_TOKEN_NAME: String,
) {
    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser =
            User(
                null,
                "testuser",
                passwordEncoder.encode("password"),
                "Test User",
                "U001",
                null,
                null,
            )
        userRepository.save(testUser)
        em.flush()
        em.clear()
    }

    @Test
    @DisplayName("성공: 유효한 정보로 회원가입 시 사용자가 생성되고 비밀번호가 암호화된다")
    fun signUp_withValidRequest_shouldCreateAndEncryptUser() {
        val request = SignUpRequest("newUser", "password123", "New User", "U002")
        val userId = authenticationService.signUp(request)
        em.flush()
        em.clear()
        val foundUser = userRepository.findWithGraphById(userId)
        Assertions.assertThat(foundUser!!.username).isEqualTo("newUser")
        Assertions.assertThat(passwordEncoder.matches("password123", foundUser.password)).isTrue()
    }

    @Test
    @DisplayName("실패: 중복된 아이디로 회원가입 시 예외가 발생한다")
    fun signUp_withDuplicateUsername_shouldThrowException() {
        val request = SignUpRequest("testuser", "password123", "Another User", "U003")
        assertThrows<CustomException> { authenticationService.signUp(request) }
    }

    @Test
    @DisplayName("성공: 올바른 자격증명으로 로그인 시 토큰이 담긴 쿠키가 발급된다")
    fun signIn_withValidCredentials_shouldPublishTokenCookies() {
        val signInRequest = SignInRequest("testuser", "password")
        val servletRequest = MockHttpServletRequest()
        val servletResponse = MockHttpServletResponse()
        authenticationService.signIn(signInRequest, servletRequest, servletResponse)
        val setCookieHeaders = servletResponse.getHeaders(HttpHeaders.SET_COOKIE)
        Assertions.assertThat(setCookieHeaders).anyMatch { it.startsWith("$ACCESS_TOKEN_NAME=") }
        Assertions.assertThat(setCookieHeaders).anyMatch { it.startsWith("$REFRESH_TOKEN_NAME=") }
        val refreshTokenValue = requireNotNull(extractTokenValueFromCookie(setCookieHeaders, REFRESH_TOKEN_NAME))
        Assertions.assertThat(refreshTokenRepository.findByToken(refreshTokenValue)).isNotNull
    }

    @Test
    @DisplayName("성공: 유효한 리프레시 토큰으로 로그아웃 시 DB에서 토큰이 삭제되고 쿠키가 만료된다")
    fun signOut_withValidRefreshToken_shouldDeleteTokenAndExpireCookies() {
        val refreshTokenValue = jwtProvider.generateRefreshToken(testUser.username)
        val refreshToken = RefreshToken.Companion.of(testUser.username, refreshTokenValue, 3600)
        refreshTokenRepository.save(refreshToken)
        em.flush()
        em.clear()

        val servletRequest = MockHttpServletRequest()
        val refreshTokenCookie = Cookie(REFRESH_TOKEN_NAME, refreshTokenValue)
        val expiryCookie = Cookie("expiry", System.currentTimeMillis().toString())
        servletRequest.setCookies(refreshTokenCookie, expiryCookie)
        val servletResponse = MockHttpServletResponse()

        authenticationService.signOut(servletRequest, servletResponse)

        Assertions.assertThat(refreshTokenRepository.findByToken(refreshTokenValue)).isNull()
        val deletedCookies = servletResponse.getHeaders(HttpHeaders.SET_COOKIE)
        Assertions
            .assertThat(deletedCookies)
            .hasSize(2)
            .anyMatch { it.startsWith("$REFRESH_TOKEN_NAME=") && it.contains("Max-Age=0") }
            .anyMatch { it.startsWith("expiry=") && it.contains("Max-Age=0") }
        Assertions.assertThat(deletedCookies).noneMatch { it.startsWith("$ACCESS_TOKEN_NAME=") }
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자로 로그인 시 예외가 발생한다")
    fun signIn_withNonExistentUser_shouldThrowException() {
        val request = SignInRequest("nonexistent", "password")
        assertThrows<CustomException> {
            authenticationService.signIn(request, MockHttpServletRequest(), MockHttpServletResponse())
        }
    }

    @Test
    @DisplayName("성공: 유효한 리프레시 토큰으로 요청 시 새로운 토큰들을 발급한다")
    fun refreshToken_withValidToken_shouldPublishNewTokens() {
        val originalRefreshToken = jwtProvider.generateRefreshToken("testuser")
        refreshTokenRepository.save(RefreshToken.Companion.of("testuser", originalRefreshToken, 3600))
        val servletRequest = MockHttpServletRequest()
        servletRequest.setCookies(Cookie(REFRESH_TOKEN_NAME, originalRefreshToken))
        val servletResponse = MockHttpServletResponse()
        authenticationService.refreshToken(servletRequest, servletResponse)
        em.flush()
        em.clear()
        val cookies = servletResponse.getHeaders(HttpHeaders.SET_COOKIE)
        Assertions.assertThat(cookies).anyMatch { it.startsWith("$ACCESS_TOKEN_NAME=") }
        Assertions.assertThat(cookies).anyMatch { it.startsWith("$REFRESH_TOKEN_NAME=") }
        val newRefreshTokenValue = requireNotNull(extractTokenValueFromCookie(cookies, REFRESH_TOKEN_NAME))
        Assertions.assertThat(refreshTokenRepository.findByToken(newRefreshTokenValue)).isNotNull
    }

    @Test
    @DisplayName("실패: 만료된 리프레시 토큰으로 요청 시 예외가 발생한다")
    fun refreshToken_withExpiredToken_shouldThrowException() {
        val expiredToken =
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY3MjUyODQwMCwiZXhwIjoxNjcyNTI4NDAwfQ.fake_expired_signature"
        val servletRequest = MockHttpServletRequest()
        servletRequest.setCookies(Cookie(REFRESH_TOKEN_NAME, expiredToken))
        assertThrows<CustomException> {
            authenticationService.refreshToken(servletRequest, MockHttpServletResponse())
        }
    }

    private fun extractTokenValueFromCookie(
        cookies: List<String>,
        cookieName: String,
    ): String? =
        cookies
            .firstOrNull { it.startsWith("$cookieName=") }
            ?.split(";")
            ?.firstOrNull()
            ?.split("=")
            ?.getOrNull(1)
}
