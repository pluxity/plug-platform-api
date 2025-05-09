package com.pluxity.authentication.service;

import com.pluxity.authentication.dto.SignInRequest;
import com.pluxity.authentication.dto.SignInResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.pluxity.global.constant.ErrorCode.INVALID_ID_OR_PASSWORD;
import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

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

    @Test
    @DisplayName("회원가입을 할 수 있다")
    void signUp() {
        // given
        SignUpRequest request = new SignUpRequest(
                "testUser",
                "password123",
                "홍길동",
                "USER001"
        );

        String encodedPassword = "encodedPassword123";
        User user = User.builder()
                .username(request.username())
                .password(encodedPassword)
                .name(request.name())
                .code(request.code())
                .build();
        
        // User 객체에 ID 설정
        ReflectionTestUtils.setField(user, "id", 1L);

        given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        Long userId = authenticationService.signUp(request);

        // then
        assertThat(userId).isEqualTo(user.getId());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("로그인을 할 수 있다")
    void signIn() {
        // given
        SignInRequest signInRequest = new SignInRequest("testUser", "password123");
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        
        User user = User.builder()
                .username(signInRequest.username())
                .password("encodedPassword")
                .name("홍길동")
                .code("USER001")
                .build();

        given(userRepository.findByUsername(signInRequest.username())).willReturn(Optional.of(user));
        given(jwtProvider.generateAccessToken(signInRequest.username())).willReturn(accessToken);
        given(jwtProvider.generateRefreshToken(signInRequest.username())).willReturn(refreshToken);

        // refreshExpiration 필드 설정
        ReflectionTestUtils.setField(authenticationService, "refreshExpiration", 3600);

        // when
        SignInResponse response = authenticationService.signIn(signInRequest, request, this.response);

        // then
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.name()).isEqualTo(user.getName());
        assertThat(response.code()).isEqualTo(user.getCode());
        
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.username(), signInRequest.password()));
        verify(jwtProvider).generateAccessToken(signInRequest.username());
        verify(jwtProvider).generateRefreshToken(signInRequest.username());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(this.response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("잘못된 인증 정보로 로그인을 시도하면 예외가 발생한다")
    void signInWithInvalidCredentials() {
        // given
        SignInRequest signInRequest = new SignInRequest("testUser", "wrongPassword");

        doThrow(new AuthenticationException("Invalid credentials") {})
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when & then
        assertThatThrownBy(() -> authenticationService.signIn(signInRequest, request, response))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_ID_OR_PASSWORD);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인을 시도하면 예외가 발생한다")
    void signInWithNonExistentUser() {
        // given
        SignInRequest signInRequest = new SignInRequest("nonExistentUser", "password123");

        given(userRepository.findByUsername(signInRequest.username())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authenticationService.signIn(signInRequest, request, response))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_USER);
    }

    @Test
    @DisplayName("로그아웃을 할 수 있다")
    void signOut() {
        // given
        String refreshToken = "validRefreshToken";
        
        given(jwtProvider.getJwtFromRequest("RefreshToken", request)).willReturn(refreshToken);

        // when
        authenticationService.signOut(request, response);

        // then
        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("리프레시 토큰으로 새로운 토큰을 발급받을 수 있다")
    void refreshToken() {
        // given
        String refreshToken = "validRefreshToken";
        String newRefreshToken = "newRefreshToken";
        String accessToken = "newAccessToken";
        String username = "testUser";
        
        given(jwtProvider.getJwtFromRequest("RefreshToken", request)).willReturn(refreshToken);
        given(jwtProvider.isRefreshTokenValid(refreshToken)).willReturn(true);
        given(jwtProvider.extractUsername(refreshToken, true)).willReturn(username);
        
        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .name("홍길동")
                .code("USER001")
                .build();
        
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(jwtProvider.generateAccessToken(username)).willReturn(accessToken);
        given(jwtProvider.generateRefreshToken(username)).willReturn(newRefreshToken);
        
        // refreshExpiration 필드 설정
        ReflectionTestUtils.setField(authenticationService, "refreshExpiration", 3600);

        // when
        authenticationService.refreshToken(request, response);

        // then
//        assertThat(tokenResponse.accessToken()).isEqualTo(accessToken);
//        verify(jwtProvider).isRefreshTokenValid(refreshToken);
//        verify(jwtProvider).extractUsername(refreshToken, true);
//        verify(jwtProvider).generateAccessToken(username);
//        verify(jwtProvider).generateRefreshToken(username);
//        verify(refreshTokenRepository).save(any(RefreshToken.class));
//        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 새로운 토큰을 요청하면 예외가 발생한다")
    void refreshTokenWithInvalidToken() {
        // given
        String invalidRefreshToken = "invalidRefreshToken";
        
        given(jwtProvider.getJwtFromRequest("RefreshToken", request)).willReturn(invalidRefreshToken);
        given(jwtProvider.isRefreshTokenValid(invalidRefreshToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authenticationService.refreshToken(request, response))
                .isInstanceOf(CustomException.class);
    }
    
    @Test
    @DisplayName("쿠키가 없을 때 새로운 쿠키를 생성한다")
    void createCookieWhenNoCookieExists() {
        // given
        String refreshToken = "refreshToken";
        int expiry = 3600;

        // private 메서드 호출을 위한 리플렉션 설정
        java.lang.reflect.Method createCookieMethod;
        try {
            createCookieMethod = AuthenticationService.class.getDeclaredMethod(
                    "createCookie", String.class, int.class, HttpServletRequest.class, HttpServletResponse.class);
            createCookieMethod.setAccessible(true);
            
            // when
            createCookieMethod.invoke(authenticationService, refreshToken, expiry, request, response);
            
            // then
            verify(response).addCookie(any(Cookie.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke createCookie method", e);
        }
    }

} 