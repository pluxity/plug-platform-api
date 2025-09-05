package com.pluxity.authentication.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.pluxity.global.exception.CustomException
import com.pluxity.global.response.ErrorResponseBody
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userDetailsService: UserDetailsService,
) : OncePerRequestFilter() {
    @Throws(ServletException::class, java.io.IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        runCatching {
            if (authenticationRequired(request)) {
                authenticateRequest(request)
            }
        }.onFailure { exception ->
            if (exception is CustomException) {
                handleAuthenticationError(response, exception)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun authenticateRequest(request: HttpServletRequest) {
        val token = jwtProvider.getAccessTokenFromRequest(request)

        if (token != null && jwtProvider.isAccessTokenValid(token)) {
            val username = jwtProvider.extractUsername(token)
            val userDetails = userDetailsService.loadUserByUsername(username)
            setAuthenticationContext(request, userDetails)
        }
    }

    private fun handleAuthenticationError(
        response: HttpServletResponse,
        exception: CustomException,
    ) {
        val objectMapper = ObjectMapper()
        response.status = exception.errorCode.getHttpStatus().value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse =
            ErrorResponseBody(
                status = exception.errorCode.getHttpStatus(),
                message = exception.message,
                code =
                    exception.errorCode
                        .getHttpStatus()
                        .value()
                        .toString(),
                error = exception.errorCode.name,
            )
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }

    private fun setAuthenticationContext(
        request: HttpServletRequest,
        userDetails: UserDetails,
    ) {
        val authToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authToken
    }

    private fun authenticationRequired(request: HttpServletRequest): Boolean {
        val path = request.requestURI.substring(request.contextPath.length)
        return WhiteListPath.entries.none { whiteListPath ->
            path.startsWith("/${whiteListPath.path}")
        }
    }
}
