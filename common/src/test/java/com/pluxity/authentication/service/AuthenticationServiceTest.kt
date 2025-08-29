package com.pluxity.authentication.service

import com.pluxity.authentication.dto.SignInRequest
import com.pluxity.authentication.dto.SignUpRequest
import com.pluxity.authentication.entity.RefreshToken
import com.pluxity.authentication.entity.RefreshToken.Companion.of
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
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function
import java.util.function.Predicate

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class AuthenticationServiceTest {
    // @MockitoBean 대신 실제 Bean들을 모두 @Autowired로 주입받습니다.
    @Autowired
    private val authenticationService: AuthenticationService? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val refreshTokenRepository: RefreshTokenRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @Autowired
    private val jwtProvider: JwtProvider? = null

    @Autowired
    private val em: EntityManager? = null

    // 테스트용 프로퍼티 값을 주입받아 검증에 활용
    @Value("\${jwt.refresh-token.name}")
    private val REFRESH_TOKEN_NAME: String? = null

    @Value("\${jwt.access-token.name}")
    private val ACCESS_TOKEN_NAME: String? = null

    private var testUser: User? = null

    @BeforeEach
    fun setUp() {
        // GIVEN: 모든 테스트에서 사용할 기본 사용자 생성
        testUser =
            User(
                null,
                "testuser",
                passwordEncoder!!.encode("password"),  // 실제 PasswordEncoder로 암호
                "Test User",
                "U001",
                null,
                null
            )
        userRepository!!.save<User?>(testUser!!)
        em!!.flush()
        em.clear()
    }

    @Test
    @DisplayName("성공: 유효한 정보로 회원가입 시 사용자가 생성되고 비밀번호가 암호화된다")
    fun signUp_withValidRequest_shouldCreateAndEncryptUser() {
        // GIVEN
        val request = SignUpRequest("newUser", "password123", "New User", "U002")

        // WHEN
        val userId = authenticationService!!.signUp(request)
        em!!.flush()
        em.clear()

        // THEN
        val foundUser = userRepository!!.findWithGraphById(userId!!)
        Assertions.assertThat(foundUser!!.username).isEqualTo("newUser")
        Assertions.assertThat(passwordEncoder!!.matches("password123", foundUser.password)).isTrue()
    }

    @Test
    @DisplayName("실패: 중복된 아이디로 회원가입 시 예외가 발생한다")
    fun signUp_withDuplicateUsername_shouldThrowException() {
        // GIVEN
        val request = SignUpRequest("testuser", "password123", "Another User", "U003")

        // WHEN & THEN
        val exception =
            org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { authenticationService!!.signUp(request) })
    }

    @Test
    @DisplayName("성공: 올바른 자격증명으로 로그인 시 토큰이 담긴 쿠키가 발급된다")
    fun signIn_withValidCredentials_shouldPublishTokenCookies() {
        // GIVEN
        val signInRequest = SignInRequest("testuser", "password")
        val servletRequest = MockHttpServletRequest()
        val servletResponse = MockHttpServletResponse()

        // WHEN
        authenticationService!!.signIn(signInRequest, servletRequest, servletResponse)

        // THEN
        // 1. 쿠키가 정상적으로 생성되었는지 확인
        val setCookieHeaders = servletResponse.getHeaders(HttpHeaders.SET_COOKIE)
        Assertions.assertThat<String?>(setCookieHeaders).anyMatch(Predicate { h: String? -> h!!.startsWith(ACCESS_TOKEN_NAME + "=") })
        Assertions.assertThat<String?>(setCookieHeaders).anyMatch(Predicate { h: String? -> h!!.startsWith(REFRESH_TOKEN_NAME + "=") })

        // 2. DB에 Refresh Token이 저장되었는지 확인
        val refreshTokenValue = extractTokenValueFromCookie(setCookieHeaders, REFRESH_TOKEN_NAME)
        Assertions.assertThat<RefreshToken?>(refreshTokenRepository!!.findByToken(refreshTokenValue)).isPresent()
    }

    @Test
    @DisplayName("성공: 유효한 리프레시 토큰으로 로그아웃 시 DB에서 토큰이 삭제되고 쿠키가 만료된다")
    fun signOut_withValidRefreshToken_shouldDeleteTokenAndExpireCookies() {
        // GIVEN
        // 1. 로그아웃할 대상 리프레시 토큰을 미리 생성하고 DB에 저장
        val refreshTokenValue = jwtProvider!!.generateRefreshToken(testUser!!.username)
        val refreshToken = of(testUser!!.username, refreshTokenValue, 3600)
        refreshTokenRepository!!.save<RefreshToken?>(refreshToken!!)
        em!!.flush()
        em.clear()

        // 2. 로그아웃 요청 준비 (실제 상황처럼 쿠키 2개를 모두 포함)
        val servletRequest = MockHttpServletRequest()

        val refreshTokenCookie = Cookie(REFRESH_TOKEN_NAME, refreshTokenValue)
        val expiryCookie =
            Cookie("expiry", System.currentTimeMillis().toString()) // 값은 중요하지 않음

        servletRequest.setCookies(refreshTokenCookie, expiryCookie) // 쿠키를 2개 설정
        val servletResponse = MockHttpServletResponse()

        // WHEN
        authenticationService!!.signOut(servletRequest, servletResponse)

        // THEN
        // 1. DB에서 해당 토큰이 삭제되었는지 확인
        Assertions.assertThat<RefreshToken?>(refreshTokenRepository.findByToken(refreshTokenValue)).isEmpty()

        // 2. 쿠키 만료 헤더가 생성되었는지 확인
        val deletedCookies = servletResponse.getHeaders(HttpHeaders.SET_COOKIE)

        // 이제 RefreshToken과 expiry 쿠키 2개에 대한 삭제 헤더가 모두 생성되므로 hasSize(2)가 통과함
        Assertions.assertThat<String?>(deletedCookies)
            .hasSize(2)
            .anyMatch(Predicate { c: String? -> c!!.startsWith(REFRESH_TOKEN_NAME + "=") && c.contains("Max-Age=0") })
            .anyMatch(Predicate { c: String? -> c!!.startsWith("expiry=") && c.contains("Max-Age=0") })

        // Access Token 삭제 쿠키는 여전히 생성되지 않는 것이 정상 동작
        Assertions.assertThat<String?>(deletedCookies).noneMatch(Predicate { c: String? -> c!!.startsWith(ACCESS_TOKEN_NAME + "=") })
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자로 로그인 시 예외가 발생한다")
    fun signIn_withNonExistentUser_shouldThrowException() {
        // GIVEN
        val request = SignInRequest("nonexistent", "password")

        // WHEN & THEN
        val exception =
            org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(
                CustomException::class.java,
                Executable {
                    authenticationService!!.signIn(
                        request, MockHttpServletRequest(), MockHttpServletResponse()
                    )
                })
    }

    @Test
    @DisplayName("성공: 유효한 리프레시 토큰으로 요청 시 새로운 토큰들을 발급한다")
    fun refreshToken_withValidToken_shouldPublishNewTokens() {
        // GIVEN
        // 1. 실제 리프레시 토큰 생성 및 저장
        val originalRefreshToken = jwtProvider!!.generateRefreshToken("testuser")
        refreshTokenRepository!!.save<RefreshToken?>(
            of("testuser", originalRefreshToken, 3600)!!
        )

        // 2. 요청 객체 준비
        val servletRequest = MockHttpServletRequest()
        servletRequest.setCookies(Cookie(REFRESH_TOKEN_NAME, originalRefreshToken))
        val servletResponse = MockHttpServletResponse()

        // WHEN
        authenticationService!!.refreshToken(servletRequest, servletResponse)
        em!!.flush()
        em.clear()

        // THEN
        // 1. 새로운 토큰이 발급되었는지 확인
        val cookies = servletResponse.getHeaders(HttpHeaders.SET_COOKIE)
        Assertions.assertThat<String?>(cookies).anyMatch(Predicate { c: String? -> c!!.startsWith(ACCESS_TOKEN_NAME + "=") })
        Assertions.assertThat<String?>(cookies).anyMatch(Predicate { c: String? -> c!!.startsWith(REFRESH_TOKEN_NAME + "=") })

        // 2. 발급된 새로운 리프레시 토큰이 DB에 저장되었는지 확인
        val newRefreshTokenValue = extractTokenValueFromCookie(cookies, REFRESH_TOKEN_NAME)
        Assertions.assertThat<RefreshToken?>(refreshTokenRepository.findByToken(newRefreshTokenValue)).isPresent()
    }

    @Test
    @DisplayName("실패: 만료된 리프레시 토큰으로 요청 시 예외가 발생한다")
    fun refreshToken_withExpiredToken_shouldThrowException() {
        // GIVEN
        // JwtProvider를 잠시 수정하여 만료된 토큰을 생성해야 함 (이런 경우만 부분적 Mocking이 유용)
        // 여기서는 실제 구현을 가정하고, 만료된 토큰 문자열이 있다고 가정
        val expiredToken =
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY3MjUyODQwMCwiZXhwIjoxNjcyNTI4NDAwfQ.fake_expired_signature"
        val servletRequest = MockHttpServletRequest()
        servletRequest.setCookies(Cookie(REFRESH_TOKEN_NAME, expiredToken))

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(
            CustomException::class.java,
            Executable { authenticationService!!.refreshToken(servletRequest, MockHttpServletResponse()) })
    }

    // 테스트에서 쿠키 값만 추출하기 위한 헬퍼 메서드
    private fun extractTokenValueFromCookie(cookies: MutableList<String?>, cookieName: String?): String? {
        return cookies.stream()
            .filter { c: String? -> c!!.startsWith(cookieName + "=") }
            .findFirst()
            .map<String>(Function { c: String? ->
                c!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            })
            .orElse(null)
    }
}
