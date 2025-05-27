package com.pluxity.domains.config;

import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Primary
@RequiredArgsConstructor
public class SasangSecurityConfig {

    private final UserRepository repository;
    private final JwtProvider jwtProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 1. GET 요청은 모두 허용하는 SecurityFilterChain
    @Bean
    @Order(1) // 우선순위 높게 설정
    public SecurityFilterChain permitGetRequestsFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(
                        new AntPathRequestMatcher("/**", HttpMethod.GET.name())) // 모든 GET 요청을 대상으로 함
                .authorizeHttpRequests(
                        authorize -> authorize.anyRequest().permitAll() // 모든 GET 요청 허용
                        )
                .csrf(AbstractHttpConfigurer::disable) // GET 요청에는 CSRF 불필요
                .cors(Customizer.withDefaults())
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 필요하다면 상태 비저장

        return http.build();
    }
}
