package com.pluxity.global.config

import com.pluxity.authentication.security.CustomUserDetails
import com.pluxity.authentication.security.JwtAuthenticationFilter
import com.pluxity.authentication.security.JwtProvider
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.user.entity.User
import com.pluxity.user.repository.UserRepository
import lombok.RequiredArgsConstructor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.function.Function
import java.util.function.Supplier

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
class CommonSecurityConfig(
    private val repository: UserRepository,
    private val jwtProvider: JwtProvider,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/actuator/**",
                        "/health",
                        "/info",
                        "/prometheus",
                        "/error",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/swagger-config/**",
                        "/docs/**",
                    ).permitAll() // .requestMatchers("/admin/**").hasRole("ADMIN") // TODO: 구현 완료 시 적용
                    .requestMatchers("/auth/**")
                    .permitAll() // GET 외의 /auth/** 경로도 허용
                    .anyRequest()
                    .authenticated()
            } // 나머지 모든 (GET이 아닌) 요청은 인증 필요
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .sessionManagement { sessionManagement: SessionManagementConfigurer<HttpSecurity> ->
                sessionManagement.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS,
                )
            }

        return http.build()
    }

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager

    @Bean
    fun userDetailsService(): UserDetailsService =
        UserDetailsService { username: String ->
            repository
                .findByUsername(username)
                .map(Function { user: User -> CustomUserDetails(user) })
                .orElseThrow(Supplier { CustomException(ErrorCode.NOT_FOUND_USER) })
        }

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter = JwtAuthenticationFilter(jwtProvider, userDetailsService())

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = mutableListOf("http://localhost:*", "http://192.168.4.8:*")
        configuration.allowedMethods = mutableListOf("GET", "PATCH", "POST", "PUT", "DELETE", "OPTIONS") // OPTIONS도 명시적으로 허용하는 것이 좋음
        configuration.allowedHeaders = mutableListOf("*") // 와일드카드 또는 필요한 헤더 명시
        configuration.allowCredentials = true
        configuration.maxAge = 3600L // pre-flight 요청 캐시 시간

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
