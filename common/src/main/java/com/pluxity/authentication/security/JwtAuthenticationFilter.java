package com.pluxity.authentication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.response.ErrorResponseBody;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        // GET 메서드인 경우 바로 필터체인 진행
        if (HttpMethod.GET.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Optional.of(request)
                    .filter(this::authenticationRequired)
                    .map(jwtProvider::getAccessTokenFromRequest)
                    .filter(jwtProvider::isAccessTokenValid)
                    .map(jwtProvider::extractUsername)
                    .map(userDetailsService::loadUserByUsername)
                    .ifPresent(userDetails -> setAuthenticationContext(request, userDetails));

        } catch (CustomException e) {

            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(e.getErrorCode().getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            var errorResponse = ErrorResponseBody.of(e.getErrorCode(), e.getErrorCode().getMessage());

            try {
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            } catch (IOException ioException) {
                log.error(ioException.getMessage());
            }

            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(HttpServletRequest request, UserDetails userDetails) {
        final UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private boolean authenticationRequired(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());

        for (WhiteListPath value : WhiteListPath.values()) {
            if (path.startsWith("/" + value.getPath())) {
                return false;
            }
        }
        return true;
    }
}
