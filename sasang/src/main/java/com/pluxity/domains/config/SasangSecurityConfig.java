package com.pluxity.domains.config;

import com.pluxity.authentication.security.CustomUserDetails;
import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${domain.name}")
    private String domainName;

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


    @Bean
    @Order(3)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 브라우저가 보내는 Origin을 정확히 명시합니다.
        configuration.setAllowedOrigins(List.of("http://" + domainName)); // "http://101.254.21.120:10300"

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // 쿠키 사용 시 필수

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource2() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
                List.of("http://localhost:*", "http://app.plug-platform:*", "http://101.254.21.120:*"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // OPTIONS도 명시적으로 허용하는 것이 좋음
        configuration.setAllowedHeaders(List.of("*")); // 와일드카드 또는 필요한 헤더 명시
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // pre-flight 요청 캐시 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
