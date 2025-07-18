package com.pluxity.authentication.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.authentication.dto.SignInRequest;
import com.pluxity.authentication.dto.SignUpRequest;
import com.pluxity.authentication.entity.RefreshToken;
import com.pluxity.authentication.repository.RefreshTokenRepository;
import com.pluxity.authentication.security.CustomUserDetails;
import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    //    @Value("${server.address}")
    @Value("${domain.name}")
    private String domainName;

    @Value("${jwt.refresh-token.expiration}")
    private int refreshExpiration;

    @Value("${jwt.access-token.expiration}")
    private int accessExpiration;

    @Value("${jwt.access-token.name}")
    private String ACCESS_TOKEN_NAME;

    @Value("${jwt.refresh-token.name}")
    private String REFRESH_TOKEN_NAME;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUp(final SignUpRequest signUpRequest) {

        userRepository
                .findByUsername(signUpRequest.username())
                .ifPresent(
                        user -> {
                            throw new CustomException(
                                    DUPLICATE_USERNAME, "사용자가 이미 존재합니다 : " + user.getUsername());
                        });

        User user =
                User.builder()
                        .username(signUpRequest.username())
                        .password(passwordEncoder.encode(signUpRequest.password()))
                        .name(signUpRequest.name())
                        .code(signUpRequest.code())
                        .build();

        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    @Transactional
    public void signIn(
            final SignInRequest signInRequestDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signInRequestDto.username(), signInRequestDto.password()));
        } catch (AuthenticationException e) {
            log.error("Invalid Id or Password : {}", e.getMessage());
            throw new CustomException(INVALID_ID_OR_PASSWORD);
        }

        User user =
                userRepository
                        .findByUsername(signInRequestDto.username())
                        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        publishToken(user, request, response);
    }

    @Transactional
    public void signOut(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = jwtProvider.getJwtFromRequest(REFRESH_TOKEN_NAME, request);

        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);

            deleteAuthCookie(ACCESS_TOKEN_NAME, request.getContextPath(), request, response);
            deleteAuthCookie(REFRESH_TOKEN_NAME, request.getContextPath() + "/", request, response);
            deleteExpiryCookie(request, response);

        } else {
            log.warn("No refresh token found for sign out");
        }
    }

    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.getJwtFromRequest(REFRESH_TOKEN_NAME, request);

        if (!jwtProvider.isRefreshTokenValid(refreshToken)) {
            log.error("Refresh Token Error :{}", refreshToken);
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }

        String username = jwtProvider.extractUsername(refreshToken, true);

        CustomUserDetails userDetails =
                userRepository
                        .findByUsername(username)
                        .map(CustomUserDetails::new)
                        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        publishToken(userDetails.user(), request, response);
    }

    private void publishToken(User user, HttpServletRequest request, HttpServletResponse response) {

        String newAccessToken = jwtProvider.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getUsername());

        createAuthCookie(
                ACCESS_TOKEN_NAME, newAccessToken, accessExpiration, request.getContextPath(), response);
        createAuthCookie(
                REFRESH_TOKEN_NAME,
                newRefreshToken,
                refreshExpiration,
                request.getContextPath() + "/",
                response);

        createExpiryCookie(request, response);

        refreshTokenRepository.save(
                RefreshToken.of(user.getUsername(), newRefreshToken, refreshExpiration));
    }

    // private helper to get domain without port
    private String getCookieDomain() {
        if (domainName != null && domainName.contains(":")) {
            return domainName.split(":")[0];
        }
        return domainName;
    }

    private void createAuthCookie(
            String name, String value, int expiry, String path, HttpServletResponse response) {

        String cookie =
                ResponseCookie.from(name, value)
                        .domain(getCookieDomain()) // 수정된 로직 사용
                        .secure(false)
                        .httpOnly(true)
                        .sameSite("Lax")
                        .maxAge(expiry)
                        .path(StringUtils.isBlank(path) ? "/" : path)
                        .build()
                        .toString();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
    }

    private void deleteAuthCookie(
            String name, String path, HttpServletRequest request, HttpServletResponse response) {

        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            cookie.setValue(null);
            cookie.setMaxAge(0);
            cookie.setDomain(getCookieDomain()); // ★★★ 여기도 수정! ★★★
            cookie.setPath(path);
            response.addCookie(cookie);
        }
    }

    private void createExpiryCookie(HttpServletRequest request, HttpServletResponse response) {
        long currentTimeMillis = System.currentTimeMillis();
        long tokenExpiryInMillis = refreshExpiration * 1000L;
        long expiryTimeMillis = currentTimeMillis + tokenExpiryInMillis;

        // 사람이 읽을 수 있는 형식으로 변환
        Instant expiryInstant = Instant.ofEpochMilli(expiryTimeMillis);
        String formattedTime =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.of("Asia/Seoul"))
                        .format(expiryInstant);

        log.info("만료 시간: {}", formattedTime);

        String path = request.getContextPath();
        String cookie =
                ResponseCookie.from("expiry", String.valueOf(expiryTimeMillis))
                        .domain(getCookieDomain()) // ★★★ 여기도 수정! ★★★
                        .secure(false)
                        .path(Optional.ofNullable(path).filter(p -> !p.isEmpty()).orElse("/"))
                        .build()
                        .toString();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
    }

    private void deleteExpiryCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "expiry");
        if (cookie != null) {
            String path = request.getContextPath();
            cookie.setMaxAge(0);
            cookie.setDomain(getCookieDomain()); // ★★★ 여기도 수정! ★★★
            cookie.setPath(Optional.ofNullable(path).filter(p -> !p.isEmpty()).orElse("/"));
            response.addCookie(cookie);
        }
    }
}
