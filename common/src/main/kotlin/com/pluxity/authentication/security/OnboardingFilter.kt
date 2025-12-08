package com.pluxity.authentication.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

class OnboardingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // 헤더 체크
        val headers = request.getHeaders("X-ONBOARDING-KEY")
        if (headers.hasMoreElements()) {
            log.info { "Hello Onboarding" }
        }

        filterChain.doFilter(request, response)
    }
}
