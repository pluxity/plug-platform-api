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
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
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
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

@Service
@Slf4j
@RequiredArgsConstructor
class AuthenticationService {
    //    @Value("${server.address}")
    @Value("\${domain.name}")
    private val domainName: String? = null

    @Value("\${jwt.refresh-token.expiration}")
    private val refreshExpiration = 0

    @Value("\${jwt.access-token.expiration}")
    private val accessExpiration = 0

    @Value("\${jwt.access-token.name}")
    private val ACCESS_TOKEN_NAME: String? = null

    @Value("\${jwt.refresh-token.name}")
    private val REFRESH_TOKEN_NAME: String? = null

    private val refreshTokenRepository: RefreshTokenRepository? = null

    private val userRepository: UserRepository? = null

    private val jwtProvider: JwtProvider? = null
    private val authenticationManager: AuthenticationManager? = null

    private val passwordEncoder: PasswordEncoder? = null

    @Transactional
    fun signUp(signUpRequest: SignUpRequest): Long? {
        userRepository!!
            .findByUsername(signUpRequest.username)
            .ifPresent(
                Consumer { user: User? ->
                    throw CustomException(
                        ErrorCode.DUPLICATE_USERNAME, "사용자가 이미 존재합니다 : " + user!!.username
                    )
                })

        val user =
            User(
                null,
                signUpRequest.username,
                passwordEncoder!!.encode(signUpRequest.password),
                signUpRequest.name,
                signUpRequest.code,
                null,
                null
            )

        val savedUser = userRepository.save<User>(user)

        return savedUser.id
    }

    @Transactional
    fun signIn(
        signInRequestDto: SignInRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        try {
            authenticationManager!!.authenticate(
                UsernamePasswordAuthenticationToken(
                    signInRequestDto.username, signInRequestDto.password
                )
            )
        } catch (e: AuthenticationException) {
            AuthenticationService.log.error("Invalid Id or Password : {}", e.message)
            throw CustomException(ErrorCode.INVALID_ID_OR_PASSWORD)
        }

        val user =
            userRepository!!
                .findByUsername(signInRequestDto.username)
                .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_USER) })

        publishToken(user, request, response)
    }

    @Transactional
    fun signOut(request: HttpServletRequest, response: HttpServletResponse) {
        val refreshToken = jwtProvider!!.getJwtFromRequest(REFRESH_TOKEN_NAME, request)

        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenRepository!!.findByToken(refreshToken).ifPresent(Consumer { entity: RefreshToken? -> refreshTokenRepository.delete(entity!!) })

            deleteAuthCookie(ACCESS_TOKEN_NAME!!, request.getContextPath(), request, response)
            deleteAuthCookie(REFRESH_TOKEN_NAME!!, request.getContextPath() + "/", request, response)
            deleteExpiryCookie(request, response)
        } else {
            AuthenticationService.log.warn("No refresh token found for sign out")
        }
    }

    @Transactional
    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse) {
        val refreshToken = jwtProvider!!.getJwtFromRequest(REFRESH_TOKEN_NAME, request)

        if (!jwtProvider.isRefreshTokenValid(refreshToken)) {
            AuthenticationService.log.error("Refresh Token Error :{}", refreshToken)
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val username = jwtProvider.extractUsername(refreshToken, true)

        val userDetails =
            userRepository!!
                .findByUsername(username)
                .map<CustomUserDetails>(Function { user: User? -> CustomUserDetails(user) })
                .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_USER) })
        publishToken(userDetails.user, request, response)
    }

    private fun publishToken(user: User, request: HttpServletRequest, response: HttpServletResponse) {
        val newAccessToken = jwtProvider!!.generateAccessToken(user.username)
        val newRefreshToken = jwtProvider.generateRefreshToken(user.username)

        createAuthCookie(
            ACCESS_TOKEN_NAME!!, newAccessToken, accessExpiration, request.getContextPath(), response
        )
        createAuthCookie(
            REFRESH_TOKEN_NAME!!,
            newRefreshToken,
            refreshExpiration,
            request.getContextPath() + "/",
            response
        )

        createExpiryCookie(request, response)

        refreshTokenRepository!!.save<RefreshToken?>(
            RefreshToken.Companion.of(user.username, newRefreshToken, refreshExpiration)
        )
    }

    private val cookieDomain: String?
        // private helper to get domain without port
        get() {
            if (domainName != null && domainName.contains(":")) {
                return domainName.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
            return domainName
        }

    private fun createAuthCookie(
        name: String, value: String, expiry: Int, path: String?, response: HttpServletResponse
    ) {
        val cookie =
            ResponseCookie.from(name, value) //                        .domain(getCookieDomain()) // 수정된 로직 사용
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
        name: String, path: String?, request: HttpServletRequest, response: HttpServletResponse
    ) {
        val cookie = WebUtils.getCookie(request, name)
        if (cookie != null) {
            cookie.setValue(null)
            cookie.setMaxAge(0)
            cookie.setDomain(this.cookieDomain) // ★★★ 여기도 수정! ★★★
            cookie.setPath(path)
            response.addCookie(cookie)
        }
    }

    private fun createExpiryCookie(request: HttpServletRequest, response: HttpServletResponse) {
        val currentTimeMillis = System.currentTimeMillis()
        val tokenExpiryInMillis = refreshExpiration * 1000L
        val expiryTimeMillis = currentTimeMillis + tokenExpiryInMillis

        // 사람이 읽을 수 있는 형식으로 변환
        val expiryInstant = Instant.ofEpochMilli(expiryTimeMillis)
        val formattedTime =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Seoul"))
                .format(expiryInstant)

        AuthenticationService.log.info("만료 시간: {}", formattedTime)
        val path = request.getContextPath()
        val cookie =
            ResponseCookie.from("expiry", expiryTimeMillis.toString()) //                        .domain(getCookieDomain()) // ★★★ 여기도 수정! ★★★
                .secure(false)
                .path(Optional.ofNullable<String?>(path).filter(Predicate { p: String? -> !p!!.isEmpty() }).orElse("/"))
                .build()
                .toString()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie)
    }

    private fun deleteExpiryCookie(request: HttpServletRequest, response: HttpServletResponse) {
        val cookie = WebUtils.getCookie(request, "expiry")
        if (cookie != null) {
            val path = request.getContextPath()
            cookie.setMaxAge(0)
            cookie.setDomain(this.cookieDomain) // ★★★ 여기도 수정! ★★★
            cookie.setPath(Optional.ofNullable<String?>(path).filter(Predicate { p: String? -> !p!!.isEmpty() }).orElse("/"))
            response.addCookie(cookie)
        }
    }
}
