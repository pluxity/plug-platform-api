package com.pluxity.authentication.service;

import com.pluxity.authentication.dto.SignInRequest;
import com.pluxity.authentication.dto.SignInResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import static com.pluxity.global.constant.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    @Value("${server.address}")
    private String serverAddress;

    @Value("${jwt.refresh-token.expiration}")
    private int refreshExpiration;

    @Value("${jwt.access-token.expiration}")
    private int accessExpiration;

    @Value("${jwt.access-token.name}")
    private String ACCESS_TOKEN;

    @Value("${jwt.refresh-token.name}")
    private String REFRESH_TOKEN;



    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUp(final SignUpRequest signUpRequest) {

        userRepository.findByUsername(signUpRequest.username())
                .ifPresent(user -> {
                    throw new CustomException(DUPLICATE_USERNAME, "사용자가 이미 존재합니다 : " + user.getUsername());
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
    public SignInResponse signIn(final SignInRequest signInRequestDto, HttpServletRequest request, HttpServletResponse response) {

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

        String accessToken = jwtProvider.generateAccessToken(user.getUsername());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUsername());

        createCookie(
                ACCESS_TOKEN,
                accessToken,
                accessExpiration,
                "/",
                request,
                response
        );

        createCookie(
                REFRESH_TOKEN,
                refreshToken,
                refreshExpiration,
                "/auth/refresh-token",
                request,
                response
        );

        refreshTokenRepository.save(
                RefreshToken.of(user.getUsername(), refreshToken, refreshExpiration));

        return SignInResponse.builder()
                .accessToken(accessToken)
                .name(user.getName())
                .code(user.getCode())
                .build();
    }

    @Transactional
    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.getJwtFromRequest(REFRESH_TOKEN, request);

        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenRepository.deleteByToken(refreshToken);

            Cookie cookie = new Cookie(REFRESH_TOKEN, null);
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
//            cookie.setDomain(serverAddress);
            cookie.setPath("/auth/refresh-token");
            response.addCookie(cookie);

        } else {
            log.warn("No refresh token found for sign out");
        }
    }

    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.getJwtFromRequest(REFRESH_TOKEN, request);

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

        User user = userDetails.user();

        String newAccessToken = jwtProvider.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getUsername());

        createCookie(
                ACCESS_TOKEN,
                newAccessToken,
                accessExpiration,
                "/",
                request,
                response
        );
        createCookie(
                REFRESH_TOKEN,
                newRefreshToken,
                refreshExpiration,
                "/auth/refresh-token",
                request,
                response
        );

        log.info("Refresh Token {}", newRefreshToken);
        refreshTokenRepository.save(RefreshToken.of(username, newRefreshToken, refreshExpiration));
    }

    private void createCookie(String name, String value, int expiry, String path,
                              HttpServletRequest request,
                              HttpServletResponse response) {

        Cookie existCookie = WebUtils.getCookie(request, name);
        if (existCookie == null) {
            Cookie cookie = new Cookie(name, value);
            cookie.setMaxAge(expiry);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
//            cookie.setDomain(serverAddress);
            cookie.setPath(path);
            response.addCookie(cookie);
        } else {
            existCookie.setValue(value);
            existCookie.setMaxAge(expiry);
            existCookie.setHttpOnly(true);
            existCookie.setSecure(false);
//            existCookie.setDomain(serverAddress);
            existCookie.setPath(path);
            response.addCookie(existCookie);
        }
    }

}
