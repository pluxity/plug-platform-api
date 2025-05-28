package com.pluxity.domains.config;

import com.pluxity.authentication.security.CustomUserDetails;
import com.pluxity.authentication.security.JwtAuthenticationFilter;
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
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Add AuthenticationEntryPoint to ensure 401 for authentication failures
                .exceptionHandling(
                        exceptions ->
                                exceptions.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    // CommonSecurityConfig와 동일한 JWT 필터 빈 추가
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, userDetailsService());
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
