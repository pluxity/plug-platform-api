package com.pluxity.authentication.service

import com.pluxity.authentication.dto.SignInRequest
import com.pluxity.authentication.dto.SignUpRequest
import com.pluxity.authentication.entity.RefreshToken
import com.pluxity.authentication.repository.RefreshTokenRepository
import com.pluxity.authentication.security.CustomUserDetails
import com.pluxity.authentication.security.JwtProvider
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.user.entity.User
import com.pluxity.user.repository.UserRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.WebUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Optional

@Service
class AuthenticationService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) {
    @Value("\${jwt.refresh-token.expiration}")
    private var refreshExpiration: Int = 0

    @Value("\${jwt.access-token.expiration}")
    private var accessExpiration: Int = 0

    @Value("\${jwt.access-token.name}")
    private lateinit var accessTokenName: String

    @Value("\${jwt.refresh-token.name}")
    private lateinit var refreshTokenName: String

    @Transactional
    fun signUp(signUpRequest: SignUpRequest): Long {
        userRepository.findByUsername(signUpRequest.username).ifPresent { user ->
            throw CustomException(ErrorCode.DUPLICATE_USERNAME, "사용자가 이미 존재합니다 : ${user.username}")
        }

        val user =
            User(
                null,
                signUpRequest.username,
                passwordEncoder.encode(signUpRequest.password),
                signUpRequest.name,
                signUpRequest.code,
                null,
                null,
            )
        val savedUser = userRepository.save(user)
        return savedUser.id!!
    }

    @Transactional
    fun signIn(
        signInRequestDto: SignInRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(signInRequestDto.username, signInRequestDto.password),
            )
        } catch (e: AuthenticationException) {
            throw CustomException(ErrorCode.INVALID_ID_OR_PASSWORD)
        }

        val user =
            userRepository
                .findByUsername(signInRequestDto.username)
                .orElseThrow { CustomException(ErrorCode.NOT_FOUND_USER) }
        publishToken(user, request, response)
    }

    @Transactional
    fun signOut(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val refreshToken = jwtProvider.getJwtFromRequest(refreshTokenName, request)
        if (!refreshToken.isNullOrEmpty()) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent { refreshTokenRepository.delete(it) }
            deleteAuthCookie(accessTokenName, request.contextPath, request, response)
            deleteAuthCookie(refreshTokenName, request.contextPath + "/", request, response)
            deleteExpiryCookie(request, response)
        }
    }

    @Transactional
    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val refreshToken = jwtProvider.getJwtFromRequest(refreshTokenName, request)
        if (!jwtProvider.isRefreshTokenValid(refreshToken)) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }
        val username = jwtProvider.extractUsername(refreshToken, true)
        val userDetails =
            userRepository
                .findByUsername(username)
                .map { CustomUserDetails(it) }
                .orElseThrow { CustomException(ErrorCode.NOT_FOUND_USER) }
        publishToken(userDetails.user, request, response)
    }

    private fun publishToken(
        user: User,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val newAccessToken = jwtProvider.generateAccessToken(user.username)
        val newRefreshToken = jwtProvider.generateRefreshToken(user.username)
        createAuthCookie(accessTokenName, newAccessToken, accessExpiration, request.contextPath, response)
        createAuthCookie(refreshTokenName, newRefreshToken, refreshExpiration, request.contextPath + "/", response)
        createExpiryCookie(request, response)
        refreshTokenRepository.save(RefreshToken.of(user.username, newRefreshToken, refreshExpiration))
    }

    private fun createAuthCookie(
        name: String,
        value: String,
        expiry: Int,
        path: String,
        response: HttpServletResponse,
    ) {
        val cookie =
            ResponseCookie
                .from(name, value)
                .secure(false)
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(expiry.toLong())
                .path(if (StringUtils.isBlank(path)) "/" else path)
                .build()
                .toString()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie)
    }

    private fun deleteAuthCookie(
        name: String,
        path: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val cookie: Cookie? = WebUtils.getCookie(request, name)
        if (cookie != null) {
            cookie.value = null
            cookie.maxAge = 0
            cookie.path = path
            response.addCookie(cookie)
        }
    }

    private fun createExpiryCookie(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val currentTimeMillis = System.currentTimeMillis()
        val tokenExpiryInMillis = refreshExpiration * 1000L
        val expiryTimeMillis = currentTimeMillis + tokenExpiryInMillis
        val formattedTime =
            DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Seoul"))
                .format(Instant.ofEpochMilli(expiryTimeMillis))
        val path = request.contextPath
        val cookie =
            ResponseCookie
                .from("expiry", expiryTimeMillis.toString())
                .secure(false)
                .path(Optional.ofNullable(path).filter { it.isNotEmpty() }.orElse("/"))
                .build()
                .toString()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie)
    }

    private fun deleteExpiryCookie(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val cookie: Cookie? = WebUtils.getCookie(request, "expiry")
        if (cookie != null) {
            val path = request.contextPath
            cookie.maxAge = 0
            cookie.path = Optional.ofNullable(path).filter { it.isNotEmpty() }.orElse("/")
            response.addCookie(cookie)
        }
    }
}
