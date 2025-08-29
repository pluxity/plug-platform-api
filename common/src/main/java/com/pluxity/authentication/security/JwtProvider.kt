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
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.WebUtils
import java.util.*
import java.util.function.Function
import java.util.function.Supplier
import javax.crypto.SecretKey

@Service
@Slf4j
class JwtProvider(private val refreshTokenRepository: RefreshTokenRepository) {
    @Value("\${jwt.access-token.name}")
    private val ACCESS_TOKEN: String? = null

    @Value("\${jwt.access-token.secret}")
    private var accessSecretKey: String? = null

    @Value("\${jwt.access-token.expiration}")
    private val accessExpiration: Long = 0

    @Value("\${jwt.refresh-token.secret}")
    private var refreshSecretKey: String? = null

    @Value("\${jwt.refresh-token.expiration}")
    private val refreshExpiration: Long = 0

    @PostConstruct
    protected fun init() {
        accessSecretKey = Base64.getEncoder().encodeToString(accessSecretKey!!.toByteArray())
        refreshSecretKey = Base64.getEncoder().encodeToString(refreshSecretKey!!.toByteArray())
    }

    @JvmOverloads
    fun extractUsername(token: String?, isRefreshToken: Boolean = false): String? {
        return extractClaim<String?>(token, Function { obj: Claims? -> obj!!.getSubject() }, isRefreshToken)
    }

    fun <T> extractClaim(
        token: String?, claimsResolver: Function<Claims?, T?>, isRefreshToken: Boolean
    ): T? {
        val claims = extractAllClaims(token, isRefreshToken)
        return claimsResolver.apply(claims)
    }

    private fun extractAllClaims(token: String?, isRefreshToken: Boolean): Claims? {
        return Jwts.parser()
            .verifyWith(getSecretKey(isRefreshToken))
            .build()
            .parseSignedClaims(token)
            .getPayload()
    }

    fun generateAccessToken(username: String?): String? {
        return generateAccessToken(HashMap<String?, Any?>(), username)
    }

    fun generateAccessToken(extraClaims: MutableMap<String?, Any?>?, username: String?): String? {
        return buildToken(extraClaims, username, accessExpiration, false)
    }

    fun generateRefreshToken(username: String?): String? {
        return buildToken(HashMap<String?, Any?>(), username, refreshExpiration, true)
    }

    private fun buildToken(
        extraClaims: MutableMap<String?, Any?>?,
        username: String?,
        expiration: Long,
        isRefreshToken: Boolean
    ): String? {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration * 1000))
            .signWith<SecretKey?>(getSecretKey(isRefreshToken), Jwts.SIG.HS256)
            .compact()
    }

    fun isAccessTokenValid(token: String?): Boolean {
        try {
            Jwts.parser().verifyWith(getSecretKey(false)).build().parseSignedClaims(token)

            return true
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN)
        } catch (e: JwtException) {
            throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN)
        } catch (e: IllegalArgumentException) {
            throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN)
        }
    }

    fun isRefreshTokenValid(token: String?): Boolean {
        try {
            val refreshToken =
                refreshTokenRepository
                    .findByToken(token)
                    .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.INVALID_REFRESH_TOKEN) })

            Jwts.parser()
                .verifyWith(getSecretKey(true))
                .build()
                .parseSignedClaims(refreshToken.getToken())

            return true
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN)
        } catch (e: JwtException) {
            println("RefreshToken :" + e.message)
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        } catch (e: IllegalArgumentException) {
            println("RefreshToken :" + e.message)
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }
    }

    private fun getSecretKey(isRefreshToken: Boolean): SecretKey {
        val keyBytes = Decoders.BASE64.decode(if (isRefreshToken) refreshSecretKey else accessSecretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun getAccessTokenFromRequest(request: HttpServletRequest): String? {
        return getJwtFromRequest(ACCESS_TOKEN!!, request)
    }

    fun getJwtFromRequest(name: String, request: HttpServletRequest): String? {
        val cookie = WebUtils.getCookie(request, name)
        if (cookie == null) {
            throw CustomException(ErrorCode.INVALID_TOKEN_FORMAT)
        }

        return cookie.getValue()
    }
}
