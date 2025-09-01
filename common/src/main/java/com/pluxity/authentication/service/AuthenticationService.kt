package com.pluxity.authentication.service

import com.pluxity.authentication.dto.SignInRequest
import com.pluxity.authentication.dto.SignUpRequest
import com.pluxity.authentication.entity.RefreshToken
import com.pluxity.authentication.repository.RefreshTokenRepository
import com.pluxity.authentication.security.JwtProvider
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.user.entity.User
import com.pluxity.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.WebUtils

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
        validateUserDoesNotExist(signUpRequest.username)

        val user =
            User(
                id = null,
                username = signUpRequest.username,
                password = passwordEncoder.encode(signUpRequest.password),
                name = signUpRequest.name,
                code = signUpRequest.code,
            )

        return userRepository.save(user).id!!
    }

    @Transactional
    fun signIn(
        signInRequest: SignInRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        authenticateUser(signInRequest)
        val user = findUserByUsername(signInRequest.username)
        publishToken(user, request, response)
    }

    @Transactional
    fun signOut(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val refreshToken = jwtProvider.getJwtFromRequest(refreshTokenName, request)
        refreshToken?.let {
            refreshTokenRepository
                .findByToken(it)
                .ifPresent { token -> refreshTokenRepository.delete(token) }
            clearAllCookies(request, response)
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

        val username = jwtProvider.extractUsername(refreshToken!!, true)
        val user = findUserByUsername(username)
        publishToken(user, request, response)
    }

    private fun validateUserDoesNotExist(username: String) {
        userRepository
            .findByUsername(username)
            .ifPresent { throw CustomException(ErrorCode.DUPLICATE_USERNAME, "사용자가 이미 존재합니다: $username") }
    }

    private fun authenticateUser(signInRequest: SignInRequest) {
        runCatching {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(signInRequest.username, signInRequest.password),
            )
        }.getOrElse {
            throw CustomException(ErrorCode.INVALID_ID_OR_PASSWORD)
        }
    }

    private fun findUserByUsername(username: String): User =
        userRepository
            .findByUsername(username)
            .orElseThrow { CustomException(ErrorCode.NOT_FOUND_USER) }

    private fun clearAllCookies(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        deleteAuthCookie(accessTokenName, request.contextPath, request, response)
        deleteAuthCookie(refreshTokenName, "${request.contextPath}/", request, response)
        deleteExpiryCookie(request, response)
    }

    private fun publishToken(
        user: User,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val newAccessToken = jwtProvider.generateAccessToken(user.username)
        val newRefreshToken = jwtProvider.generateRefreshToken(user.username)

        createAuthCookie(accessTokenName, newAccessToken, accessExpiration, request.contextPath, response)
        createAuthCookie(refreshTokenName, newRefreshToken, refreshExpiration, "${request.contextPath}/", response)
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
                .path(path.takeIf { it.isNotBlank() } ?: "/")
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
        WebUtils.getCookie(request, name)?.apply {
            value = null
            maxAge = 0
            this.path = path
            response.addCookie(this)
        }
    }

    private fun createExpiryCookie(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val expiryTimeMillis = System.currentTimeMillis() + (refreshExpiration * 1000L)
        val path = request.contextPath.takeIf { it.isNotEmpty() } ?: "/"

        val cookie =
            ResponseCookie
                .from("expiry", expiryTimeMillis.toString())
                .secure(false)
                .path(path)
                .build()
                .toString()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie)
    }

    private fun deleteExpiryCookie(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        WebUtils.getCookie(request, "expiry")?.apply {
            val path = request.contextPath.takeIf { it.isNotEmpty() } ?: "/"
            maxAge = 0
            this.path = path
            response.addCookie(this)
        }
    }
}
