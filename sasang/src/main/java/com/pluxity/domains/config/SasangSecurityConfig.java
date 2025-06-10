package com.pluxity.domains.config;

import com.pluxity.authentication.security.CustomUserDetails;
import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Primary
@RequiredArgsConstructor
public class SasangSecurityConfig {

    private final UserRepository repository;
    private final JwtProvider jwtProvider;

    @Bean
    @Order(1)
    public SecurityFilterChain permitGetRequestsFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(new AntPathRequestMatcher("/**", HttpMethod.GET.name()))
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers(new AntPathRequestMatcher("/users/me", HttpMethod.GET.name()))
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // JWT 필터 추가 - 새로운 SasangJwtAuthenticationFilter 사용
                .addFilterBefore(
                        sasangJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Add AuthenticationEntryPoint to ensure 401 for authentication failures
                .exceptionHandling(
                        exceptions ->
                                exceptions.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    // GET 이외의 모든 요청에 대한 필터체인 (POST, PUT, DELETE 등)
    @Bean
    @Order(2)
    public SecurityFilterChain nonGetRequestsFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(request -> !HttpMethod.GET.matches(request.getMethod()))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                new AntPathRequestMatcher("/actuator/**"),
                                                new AntPathRequestMatcher("/health"),
                                                new AntPathRequestMatcher("/info"),
                                                new AntPathRequestMatcher("/prometheus"),
                                                new AntPathRequestMatcher("/error"),
                                                new AntPathRequestMatcher("/swagger-ui/**"),
                                                new AntPathRequestMatcher("/swagger-ui.html"),
                                                new AntPathRequestMatcher("/api-docs/**"),
                                                new AntPathRequestMatcher("/swagger-config/**"),
                                                new AntPathRequestMatcher("/docs/**"),
                                                new AntPathRequestMatcher("/open/**"))
                                        .permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/auth/**"))
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .addFilterBefore(
                        sasangJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exceptions ->
                                exceptions.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    // 기존 JwtAuthenticationFilter 대신 SasangJwtAuthenticationFilter 빈 추가
    @Bean
    public SasangJwtAuthenticationFilter sasangJwtAuthenticationFilter() {
        return new SasangJwtAuthenticationFilter(jwtProvider, userDetailsService());
    }

    // UserDetailsService 빈도 필요
    @Bean
    public UserDetailsService userDetailsService() {
        return username ->
                repository
                        .findByUsername(username)
                        .map(CustomUserDetails::new)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }
}
