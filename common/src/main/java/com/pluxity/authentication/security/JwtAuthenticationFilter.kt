package com.pluxity.authentication.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.pluxity.global.exception.CustomException
import com.pluxity.global.response.ErrorResponseBody
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

@Slf4j
@RequiredArgsConstructor
class JwtAuthenticationFilter : OncePerRequestFilter() {
    private val jwtProvider: JwtProvider? = null
    private val userDetailsService: UserDetailsService? = null

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            Optional.of<HttpServletRequest?>(request)
                .filter(Predicate { request: HttpServletRequest? -> this.authenticationRequired(request!!) })
                .map<String?>(Function { request: HttpServletRequest? -> jwtProvider!!.getAccessTokenFromRequest(request) })
                .filter(Predicate { token: String? -> jwtProvider!!.isAccessTokenValid(token) })
                .map<String?>(Function { token: String? -> jwtProvider!!.extractUsername(token) })
                .map<UserDetails?>(Function { username: String? -> userDetailsService!!.loadUserByUsername(username) })
                .ifPresent(Consumer { userDetails: UserDetails? -> setAuthenticationContext(request, userDetails!!) })
        } catch (e: CustomException) {
            val objectMapper = ObjectMapper()
            response.setStatus(e.getErrorCode().getHttpStatus().value())
            response.setContentType(MediaType.APPLICATION_JSON_VALUE)
            response.setCharacterEncoding("UTF-8")

            val errorResponse = ErrorResponseBody.of(e.getErrorCode().getHttpStatus(), e.message)

            try {
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse))
            } catch (ioException: IOException) {
                JwtAuthenticationFilter.log.error(ioException.message)
            }

            return
        }

        filterChain.doFilter(request, response)
    }

    private fun setAuthenticationContext(request: HttpServletRequest?, userDetails: UserDetails) {
        val authToken =
            UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())

        authToken.setDetails(WebAuthenticationDetailsSource().buildDetails(request))
        SecurityContextHolder.getContext().setAuthentication(authToken)
    }

    private fun authenticationRequired(request: HttpServletRequest): Boolean {
        val contextPath = request.getContextPath()
        val path = request.getRequestURI().substring(contextPath.length)

        for (value in WhiteListPath.entries) {
            if (path.startsWith("/" + value.getPath())) {
                return false
            }
        }
        return true
    }
}
