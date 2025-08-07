package com.pluxity.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.authentication.dto.SignInRequest;
import com.pluxity.authentication.dto.SignUpRequest;
import com.pluxity.authentication.entity.RefreshToken;
import com.pluxity.authentication.repository.RefreshTokenRepository;
import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthenticationServiceTest {

    // @MockBean 대신 실제 Bean들을 모두 @Autowired로 주입받습니다.
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private EntityManager em;

    // 테스트용 프로퍼티 값을 주입받아 검증에 활용
    @Value("${jwt.refresh-token.name}")
    private String REFRESH_TOKEN_NAME;
    @Value("${jwt.access-token.name}")
    private String ACCESS_TOKEN_NAME;

    private User testUser;

    @BeforeEach
    void setUp() {
        // GIVEN: 모든 테스트에서 사용할 기본 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password")) // 실제 PasswordEncoder로 암호화
                .name("Test User")
                .code("U001")
                .build();
        userRepository.save(testUser);
        em.flush();
        em.clear();
    }

        @Test
        @DisplayName("성공: 유효한 정보로 회원가입 시 사용자가 생성되고 비밀번호가 암호화된다")
        void signUp_withValidRequest_shouldCreateAndEncryptUser() {
            // GIVEN
            SignUpRequest request = new SignUpRequest("newUser", "password123", "New User", "U002");

            // WHEN
            Long userId = authenticationService.signUp(request);
            em.flush();
            em.clear();

            // THEN
            User foundUser = userRepository.findById(userId).orElseThrow();
            assertThat(foundUser.getUsername()).isEqualTo("newUser");
            assertThat(passwordEncoder.matches("password123", foundUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("실패: 중복된 아이디로 회원가입 시 예외가 발생한다")
        void signUp_withDuplicateUsername_shouldThrowException() {
            // GIVEN
            SignUpRequest request = new SignUpRequest("testuser", "password123", "Another User", "U003");

            // WHEN & THEN
            CustomException exception = assertThrows(CustomException.class, () -> authenticationService.signUp(request));
        }

    @Test
    @DisplayName("성공: 올바른 자격증명으로 로그인 시 토큰이 담긴 쿠키가 발급된다")
    void signIn_withValidCredentials_shouldPublishTokenCookies() {
        // GIVEN
        SignInRequest signInRequest = new SignInRequest("testuser", "password");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // WHEN
        authenticationService.signIn(signInRequest, servletRequest, servletResponse);

        // THEN
        // 1. 쿠키가 정상적으로 생성되었는지 확인
        List<String> setCookieHeaders = servletResponse.getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeaders).anyMatch(h -> h.startsWith(ACCESS_TOKEN_NAME + "="));
        assertThat(setCookieHeaders).anyMatch(h -> h.startsWith(REFRESH_TOKEN_NAME + "="));

        // 2. DB에 Refresh Token이 저장되었는지 확인
        String refreshTokenValue = extractTokenValueFromCookie(setCookieHeaders, REFRESH_TOKEN_NAME);
        assertThat(refreshTokenRepository.findByToken(refreshTokenValue)).isPresent();
    }

    @Test
    @DisplayName("성공: 유효한 리프레시 토큰으로 로그아웃 시 DB에서 토큰이 삭제되고 쿠키가 만료된다")
    void signOut_withValidRefreshToken_shouldDeleteTokenAndExpireCookies() {
        // GIVEN
        // 1. 로그아웃할 대상 리프레시 토큰을 미리 생성하고 DB에 저장
        String refreshTokenValue = jwtProvider.generateRefreshToken(testUser.getUsername());
        RefreshToken refreshToken = RefreshToken.of(testUser.getUsername(), refreshTokenValue, 3600);
        refreshTokenRepository.save(refreshToken);
        em.flush();
        em.clear();

        // 2. 로그아웃 요청 준비 (실제 상황처럼 쿠키 2개를 모두 포함)
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_NAME, refreshTokenValue);
        Cookie expiryCookie = new Cookie("expiry", String.valueOf(System.currentTimeMillis())); // 값은 중요하지 않음

        servletRequest.setCookies(refreshTokenCookie, expiryCookie); // 쿠키를 2개 설정
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // WHEN
        authenticationService.signOut(servletRequest, servletResponse);

        // THEN
        // 1. DB에서 해당 토큰이 삭제되었는지 확인
        assertThat(refreshTokenRepository.findByToken(refreshTokenValue)).isEmpty();

        // 2. 쿠키 만료 헤더가 생성되었는지 확인
        List<String> deletedCookies = servletResponse.getHeaders(HttpHeaders.SET_COOKIE);

        // 이제 RefreshToken과 expiry 쿠키 2개에 대한 삭제 헤더가 모두 생성되므로 hasSize(2)가 통과함
        assertThat(deletedCookies)
                .hasSize(2)
                .anyMatch(c -> c.startsWith(REFRESH_TOKEN_NAME + "=") && c.contains("Max-Age=0"))
                .anyMatch(c -> c.startsWith("expiry=") && c.contains("Max-Age=0"));

        // Access Token 삭제 쿠키는 여전히 생성되지 않는 것이 정상 동작
        assertThat(deletedCookies)
                .noneMatch(c -> c.startsWith(ACCESS_TOKEN_NAME + "="));
    }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자로 로그인 시 예외가 발생한다")
        void signIn_withNonExistentUser_shouldThrowException() {
            // GIVEN
            SignInRequest request = new SignInRequest("nonexistent", "password");

            // WHEN & THEN
            CustomException exception = assertThrows(CustomException.class, () ->
                    authenticationService.signIn(request, new MockHttpServletRequest(), new MockHttpServletResponse())
            );
        }

        @Test
        @DisplayName("성공: 유효한 리프레시 토큰으로 요청 시 새로운 토큰들을 발급한다")
        void refreshToken_withValidToken_shouldPublishNewTokens() {
            // GIVEN
            // 1. 실제 리프레시 토큰 생성 및 저장
            String originalRefreshToken = jwtProvider.generateRefreshToken("testuser");
            refreshTokenRepository.save(
                    com.pluxity.authentication.entity.RefreshToken.of("testuser", originalRefreshToken, 3600)
            );

            // 2. 요청 객체 준비
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setCookies(new Cookie(REFRESH_TOKEN_NAME, originalRefreshToken));
            MockHttpServletResponse servletResponse = new MockHttpServletResponse();

            // WHEN
            authenticationService.refreshToken(servletRequest, servletResponse);
            em.flush();
            em.clear();

            // THEN
            // 1. 새로운 토큰이 발급되었는지 확인
            List<String> cookies = servletResponse.getHeaders(HttpHeaders.SET_COOKIE);
            assertThat(cookies).anyMatch(c -> c.startsWith(ACCESS_TOKEN_NAME + "="));
            assertThat(cookies).anyMatch(c -> c.startsWith(REFRESH_TOKEN_NAME + "="));

            // 2. 발급된 새로운 리프레시 토큰이 DB에 저장되었는지 확인
            String newRefreshTokenValue = extractTokenValueFromCookie(cookies, REFRESH_TOKEN_NAME);
            assertThat(refreshTokenRepository.findByToken(newRefreshTokenValue)).isPresent();
        }

        @Test
        @DisplayName("실패: 만료된 리프레시 토큰으로 요청 시 예외가 발생한다")
        void refreshToken_withExpiredToken_shouldThrowException() {
            // GIVEN
            // JwtProvider를 잠시 수정하여 만료된 토큰을 생성해야 함 (이런 경우만 부분적 Mocking이 유용)
            // 여기서는 실제 구현을 가정하고, 만료된 토큰 문자열이 있다고 가정
            String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY3MjUyODQwMCwiZXhwIjoxNjcyNTI4NDAwfQ.fake_expired_signature";
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setCookies(new Cookie(REFRESH_TOKEN_NAME, expiredToken));

            // WHEN & THEN
            assertThrows(CustomException.class, () ->
                    authenticationService.refreshToken(servletRequest, new MockHttpServletResponse())
            );
        }

    // 테스트에서 쿠키 값만 추출하기 위한 헬퍼 메서드
    private String extractTokenValueFromCookie(List<String> cookies, String cookieName) {
        return cookies.stream()
                .filter(c -> c.startsWith(cookieName + "="))
                .findFirst()
                .map(c -> c.split(";")[0].split("=")[1])
                .orElse(null);
    }
}