package com.pluxity.authentication.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.authentication.dto.SignInRequestDto;
import com.pluxity.authentication.dto.SignInResponseDto;
import com.pluxity.authentication.dto.SignUpRequestDto;
import com.pluxity.authentication.entity.RefreshToken;
import com.pluxity.authentication.repository.RefreshTokenRepository;
import com.pluxity.authentication.security.CustomUserDetails;
import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Transactional
    public void signUp(final SignUpRequestDto signUpRequestDto) {

        var username = signUpRequestDto.username();
        userRepository
                .findByUsername(username)
                .ifPresent(
                        user -> {
                            throw new CustomException(DUPLICATE_USERNAME);
                        });

        var user =
                User.builder()
                        .username(signUpRequestDto.username())
                        .password(passwordEncoder.encode(signUpRequestDto.password()))
                        .name(signUpRequestDto.name())
                        .code(signUpRequestDto.code())
                        .role(signUpRequestDto.role())
                        .build();

        userRepository.save(user);
    }

    private boolean validateEmail(String email) {
        var regexPattern =
                "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        return Pattern.compile(regexPattern).matcher(email).matches();
    }

    @Transactional
    public SignInResponseDto signIn(final SignInRequestDto signInRequestDto) {

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

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String jwtToken = jwtProvider.generateAccessToken(userDetails);
        String refreshToken = jwtProvider.generateRefreshToken(userDetails);

        refreshTokenRepository.save(
                RefreshToken.of(user.getUsername(), refreshToken, refreshExpiration));

        return SignInResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .name(user.getName())
                .code(user.getCode())
                .build();
    }

    public SignInResponseDto refreshToken(HttpServletRequest request) {

        String refreshToken = jwtProvider.getJwtFromRequest(request);

        String accessToken;
        String newRefreshToken;

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

        accessToken = jwtProvider.generateAccessToken(userDetails);
        newRefreshToken = jwtProvider.generateRefreshToken(userDetails);

        refreshTokenRepository.save(RefreshToken.of(username, newRefreshToken, refreshExpiration));

        return SignInResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .name(user.getName())
                .code(user.getCode())
                .build();
    }
}
