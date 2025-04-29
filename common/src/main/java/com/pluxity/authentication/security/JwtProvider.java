package com.pluxity.authentication.security;

import com.pluxity.authentication.entity.RefreshToken;
import com.pluxity.authentication.repository.RefreshTokenRepository;
import com.pluxity.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.pluxity.global.constant.ErrorCode.*;

@Service
@Slf4j
public class JwtProvider {

    @Value("${jwt.access-token.name}")
    private String ACCESS_TOKEN;

    @Value("${jwt.access-token.secret}")
    private String accessSecretKey;

    @Value("${jwt.access-token.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-token.secret}")
    private String refreshSecretKey;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtProvider(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostConstruct
    protected void init() {
        accessSecretKey = Base64.getEncoder().encodeToString(accessSecretKey.getBytes());
        refreshSecretKey = Base64.getEncoder().encodeToString(refreshSecretKey.getBytes());
    }

    public String extractUsername(String token) {
        return extractUsername(token, false);
    }

    public String extractUsername(String token, boolean isRefreshToken) {
        return extractClaim(token, Claims::getSubject, isRefreshToken);
    }

    public <T> T extractClaim(
            String token, Function<Claims, T> claimsResolver, final boolean isRefreshToken) {
        final Claims claims = extractAllClaims(token, isRefreshToken);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, final boolean isRefreshToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey(isRefreshToken))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateAccessToken(String username) {
        return generateAccessToken(new HashMap<>(), username);
    }

    public String generateAccessToken(Map<String, Object> extraClaims, String username) {
        return buildToken(extraClaims, username, accessExpiration, false);
    }

    public String generateRefreshToken(String username) {
        return buildToken(new HashMap<>(), username, refreshExpiration, true);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String username,
            long expiration,
            final boolean isRefreshToken) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSecretKey(isRefreshToken), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isAccessTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(getSecretKey(false)).build().parseSignedClaims(token);

            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(EXPIRED_ACCESS_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(INVALID_ACCESS_TOKEN);
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {

            RefreshToken refreshToken =
                    refreshTokenRepository
                            .findByToken(token)
                            .orElseThrow(() -> new CustomException(INVALID_REFRESH_TOKEN));

            Jwts.parser()
                    .verifyWith(getSecretKey(true))
                    .build()
                    .parseSignedClaims(refreshToken.getToken());

            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("RefreshToken :" + e.getMessage());
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }
    }

    private SecretKey getSecretKey(final boolean isRefreshToken) {
        byte[] keyBytes = Decoders.BASE64.decode(isRefreshToken ? refreshSecretKey : accessSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getAccessTokenFromRequest(HttpServletRequest request) {
        return getJwtFromRequest(ACCESS_TOKEN, request);
    }

    public String getJwtFromRequest(String name, HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if(cookie == null) {
            throw new CustomException(INVALID_TOKEN_FORMAT);
        }

        return cookie.getValue();
    }
}
