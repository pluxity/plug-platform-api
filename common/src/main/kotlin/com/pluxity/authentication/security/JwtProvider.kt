package com.pluxity.authentication.security

import com.pluxity.authentication.repository.RefreshTokenRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.WebUtils
import java.util.Base64
import java.util.Date
import java.util.function.Function
import javax.crypto.SecretKey

@Service
class JwtProvider(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Value("\${jwt.access-token.name}")
    private lateinit var accessTokenName: String

    @Value("\${jwt.access-token.secret}")
    private lateinit var accessSecretKey: String

    @Value("\${jwt.access-token.expiration}")
    private var accessExpiration: Long = 0

    @Value("\${jwt.refresh-token.secret}")
    private lateinit var refreshSecretKey: String

    @Value("\${jwt.refresh-token.expiration}")
    private var refreshExpiration: Long = 0

    @PostConstruct
    private fun init() {
        accessSecretKey = Base64.getEncoder().encodeToString(accessSecretKey.toByteArray())
        refreshSecretKey = Base64.getEncoder().encodeToString(refreshSecretKey.toByteArray())
    }

    fun extractUsername(
        token: String,
        isRefreshToken: Boolean = false,
    ): String = extractClaim(token, Claims::getSubject, isRefreshToken)

    fun <T> extractClaim(
        token: String,
        claimsResolver: Function<Claims, T>,
        isRefreshToken: Boolean,
    ): T = claimsResolver.apply(extractAllClaims(token, isRefreshToken))

    private fun extractAllClaims(
        token: String,
        isRefreshToken: Boolean,
    ): Claims =
        Jwts
            .parser()
            .verifyWith(getSecretKey(isRefreshToken))
            .build()
            .parseSignedClaims(token)
            .payload

    fun generateAccessToken(
        username: String,
        extraClaims: Map<String, Any> = emptyMap(),
    ): String = buildToken(extraClaims, username, accessExpiration, false)

    fun generateRefreshToken(username: String): String = buildToken(emptyMap(), username, refreshExpiration, true)

    private fun buildToken(
        extraClaims: Map<String, Any>,
        username: String,
        expiration: Long,
        isRefreshToken: Boolean,
    ): String =
        Jwts
            .builder()
            .claims(extraClaims)
            .subject(username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(getSecretKey(isRefreshToken), Jwts.SIG.HS256)
            .compact()

    fun isAccessTokenValid(token: String): Boolean =
        runCatching {
            Jwts
                .parser()
                .verifyWith(getSecretKey(false))
                .build()
                .parseSignedClaims(token)
        }.fold(
            onSuccess = { true },
            onFailure = { exception ->
                when (exception) {
                    is ExpiredJwtException -> throw CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN)
                    is JwtException, is IllegalArgumentException ->
                        throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN)
                    else -> throw exception
                }
            },
        )

    fun isRefreshTokenValid(token: String?): Boolean {
        if (token.isNullOrBlank()) return false

        return runCatching {
            val refreshToken =
                refreshTokenRepository
                    .findByToken(token)
                    ?: throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)

            if (!refreshToken.isValidToken()) {
                throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
            }

            Jwts
                .parser()
                .verifyWith(getSecretKey(true))
                .build()
                .parseSignedClaims(refreshToken.token)
        }.fold(
            onSuccess = { true },
            onFailure = { exception ->
                when (exception) {
                    is ExpiredJwtException -> throw CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN)
                    is JwtException, is IllegalArgumentException ->
                        throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
                    else -> throw exception
                }
            },
        )
    }

    private fun getSecretKey(isRefreshToken: Boolean): SecretKey {
        val keyBytes = Decoders.BASE64.decode(if (isRefreshToken) refreshSecretKey else accessSecretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun getAccessTokenFromRequest(request: HttpServletRequest): String? = getJwtFromRequest(accessTokenName, request)

    fun getJwtFromRequest(
        name: String,
        request: HttpServletRequest,
    ): String? = WebUtils.getCookie(request, name)?.value
}
