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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                                        .anyRequest()
                                        .permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SasangJwtAuthenticationFilter sasangJwtAuthenticationFilter() {
        return new SasangJwtAuthenticationFilter(jwtProvider, userDetailsService());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username ->
                repository
                        .findByUsername(username)
                        .map(CustomUserDetails::new)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }
}