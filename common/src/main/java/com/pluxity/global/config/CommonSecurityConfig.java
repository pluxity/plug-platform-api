package com.pluxity.global.config;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_USER;

import com.pluxity.authentication.security.CustomUserDetails;
import com.pluxity.authentication.security.JwtAuthenticationFilter;
import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class CommonSecurityConfig {

    private final UserRepository repository;
    private final JwtProvider jwtProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. GET 이외의 요청 및 기존 규칙을 처리하는 SecurityFilterChain
    @Bean
    @Order(2) // GET 필터 체인보다 낮은 우선순위
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 이 필터 체인은 GET 요청을 제외한 모든 요청 또는 특정 경로의 모든 메소드에 적용될 수 있습니다.
                // 여기서는 명시적으로 actuator, swagger, auth 경로 등을 다시 정의하고, 나머지는 인증을 요구합니다.
                // GET 요청은 이미 필터 체인에서 처리되었으므로, 여기서는 그 외의 메소드에 대한 규칙이 주로 적용됩니다.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                // GET이 아닌 다른 메소드로 이 경로들에 접근 시 허용 (예: POST /auth/login)
                                                // 또는, 모든 메소드에 대해 허용하되, GET은 이미  permitAll 처리됨
                                                new AntPathRequestMatcher("/actuator/**"),
                                                new AntPathRequestMatcher("/health"),
                                                new AntPathRequestMatcher("/info"),
                                                new AntPathRequestMatcher("/prometheus"),
                                                new AntPathRequestMatcher("/error"),
                                                new AntPathRequestMatcher("/swagger-ui/**"),
                                                new AntPathRequestMatcher("/swagger-ui.html"),
                                                new AntPathRequestMatcher("/api-docs/**"),
                                                new AntPathRequestMatcher("/swagger-config/**"),
                                                new AntPathRequestMatcher("/docs/**"))
                                        .permitAll()
                                        // .requestMatchers("/admin/**").hasRole("ADMIN") // TODO: 구현 완료 시 적용
                                        .requestMatchers(new AntPathRequestMatcher("/auth/**"))
                                        .permitAll() // GET 외의 /auth/** 경로도 허용
                                        .anyRequest()
                                        .authenticated() // 나머지 모든 (GET이 아닌) 요청은 인증 필요
                        )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(
                        sessionManagement ->
                                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username ->
                repository
                        .findByUsername(username)
                        .map(CustomUserDetails::new)
                        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, userDetailsService());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
                List.of("http://localhost:*", "http://app.plug-platform:*"));
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
