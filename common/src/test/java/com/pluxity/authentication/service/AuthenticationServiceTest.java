package com.pluxity.authentication.service;

import com.pluxity.authentication.dto.SignInRequest;
import com.pluxity.authentication.dto.SignUpRequest;
import com.pluxity.authentication.entity.RefreshToken;
import com.pluxity.authentication.repository.RefreshTokenRepository;
import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.WebUtils;

import java.util.Optional;

import static com.pluxity.global.constant.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final String ACCESS_TOKEN_NAME = "access_token";
    private final String REFRESH_TOKEN_NAME = "refresh_token";
    private final String DOMAIN_NAME = "pluxity.com";
    private final int ACCESS_EXPIRATION = 1800;
    private final int REFRESH_EXPIRATION = 604800;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "domainName", DOMAIN_NAME);
        ReflectionTestUtils.setField(authenticationService, "accessExpiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(authenticationService, "refreshExpiration", REFRESH_EXPIRATION);
        ReflectionTestUtils.setField(authenticationService, "ACCESS_TOKEN_NAME", ACCESS_TOKEN_NAME);
        ReflectionTestUtils.setField(authenticationService, "REFRESH_TOKEN_NAME", REFRESH_TOKEN_NAME);
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        SignUpRequest request = new SignUpRequest("testuser", "password123", "테스트유저", "CODE123");
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("테스트유저")
                .code("CODE123")
                .build();

        given(userRepository.findByUsername(request.username())).willReturn(Optional.empty());
        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        Long userId = authenticationService.signUp(request);

        // then
        assertEquals("testuser", user.getUsername());
        verify(userRepository).findByUsername(request.username());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 사용자명")
    void signUp_Fail_DuplicateUsername() {
        // given
        SignUpRequest request = new SignUpRequest("testuser", "password123", "테스트유저", "CODE123");
        User existingUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("기존유저")
                .code("CODE456")
                .build();

        given(userRepository.findByUsername(request.username())).willReturn(Optional.of(existingUser));

        // when and then
        CustomException exception = assertThrows(CustomException.class, () -> authenticationService.signUp(request));
        assertEquals(DUPLICATE_USERNAME, exception.getErrorCode());
        verify(userRepository).findByUsername(request.username());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void signIn_Success() {
        // given
        SignInRequest signInRequest = new SignInRequest("testuser", "password123");
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("테스트유저")
                .code("CODE123")
                .build();

        given(userRepository.findByUsername(signInRequest.username())).willReturn(Optional.of(user));
        given(jwtProvider.generateAccessToken(user.getUsername())).willReturn("access-token-value");
        given(jwtProvider.generateRefreshToken(user.getUsername())).willReturn("refresh-token-value");
        doNothing().when(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        given(request.getContextPath()).willReturn("");

        // when
        authenticationService.signIn(signInRequest, request, response);

        // then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(signInRequest.username());
        verify(jwtProvider).generateAccessToken(user.getUsername());
        verify(jwtProvider).generateRefreshToken(user.getUsername());
        verify(response, times(3)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void signIn_Fail_InvalidPassword() {
        // given
        SignInRequest signInRequest = new SignInRequest("testuser", "wrongpassword");

        doThrow(new BadCredentialsException("Invalid credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when and then
        CustomException exception = assertThrows(CustomException.class,
                () -> authenticationService.signIn(signInRequest, request, response));
        assertEquals(INVALID_ID_OR_PASSWORD, exception.getErrorCode());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(response, never()).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void signOut_Success() {
        // given
        String refreshToken = "refresh-token-value";
        RefreshToken token = RefreshToken.of("testuser", refreshToken, REFRESH_EXPIRATION);

        given(jwtProvider.getJwtFromRequest(REFRESH_TOKEN_NAME, request)).willReturn(refreshToken);
        given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(token));
        given(request.getContextPath()).willReturn("");

        // WebUtils.getCookie 모킹
        try (MockedStatic<WebUtils> webUtilsMock = mockStatic(WebUtils.class)) {
            webUtilsMock.when(() -> WebUtils.getCookie(eq(request), eq(ACCESS_TOKEN_NAME)))
                    .thenReturn(new Cookie(ACCESS_TOKEN_NAME, "access-value"));
            webUtilsMock.when(() -> WebUtils.getCookie(eq(request), eq(REFRESH_TOKEN_NAME)))
                    .thenReturn(new Cookie(REFRESH_TOKEN_NAME, "refresh-value"));
            webUtilsMock.when(() -> WebUtils.getCookie(eq(request), eq("expiry")))
                    .thenReturn(new Cookie("expiry", "expiry-value"));

            // when
            authenticationService.signOut(request, response);
        }

        // then
        verify(jwtProvider).getJwtFromRequest(REFRESH_TOKEN_NAME, request);
        verify(refreshTokenRepository).findByToken(refreshToken);
        verify(refreshTokenRepository).delete(token);
        verify(response, times(3)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("RefreshToken 재발행 성공")
    void refreshToken_Success() {
        // given
        String username = "testuser";
        String refreshToken = "refresh-token-value";
        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .name("테스트유저")
                .code("CODE123")
                .build();

        given(jwtProvider.getJwtFromRequest(REFRESH_TOKEN_NAME, request)).willReturn(refreshToken);
        given(jwtProvider.isRefreshTokenValid(refreshToken)).willReturn(true);
        given(jwtProvider.extractUsername(refreshToken, true)).willReturn(username);
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(jwtProvider.generateAccessToken(username)).willReturn("new-access-token");
        given(jwtProvider.generateRefreshToken(username)).willReturn("new-refresh-token");
        given(request.getContextPath()).willReturn("");
        doNothing().when(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());

        // when
        authenticationService.refreshToken(request, response);

        // then
        verify(jwtProvider).getJwtFromRequest(REFRESH_TOKEN_NAME, request);
        verify(jwtProvider).isRefreshTokenValid(refreshToken);
        verify(jwtProvider).extractUsername(refreshToken, true);
        verify(userRepository).findByUsername(username);
        verify(jwtProvider).generateAccessToken(username);
        verify(jwtProvider).generateRefreshToken(username);
        verify(response, times(3)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("RefreshToken 재발행 실패 - Cookie에 RefreshToken 누락")
    void refreshToken_Fail_MissingRefreshToken() {
        // given
        given(jwtProvider.getJwtFromRequest(REFRESH_TOKEN_NAME, request)).willReturn(null);

        // when and then
        CustomException exception = assertThrows(CustomException.class,
                () -> authenticationService.refreshToken(request, response));
        assertEquals(INVALID_REFRESH_TOKEN, exception.getErrorCode());
        verify(jwtProvider).getJwtFromRequest(REFRESH_TOKEN_NAME, request);
        verify(jwtProvider, never()).isRefreshTokenValid(anyString());
        verify(userRepository, never()).findByUsername(anyString());
    }
}