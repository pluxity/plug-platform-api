package com.pluxity.domains.config;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class CorsConfig {

    // 1. 허용할 호스트(도메인, IP) 목록을 정의합니다. 포트는 신경 쓰지 않습니다.
    private final List<String> allowedHosts =
            List.of(
                    "192.168.4.56",
                    "192.168.4.8",
                    "192.168.4.31",
                    "datahub.com",
                    "localhost",
                    "101.254.21.120",
                    "data.hubmetro.bsan.kr");

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration configuration = new CorsConfiguration();

                // 2. 요청 헤더에서 Origin 정보를 가져옵니다.
                String origin = request.getHeader("Origin");

                // 3. Origin 헤더가 있고, 우리가 허용한 호스트 목록에 포함되는지 확인합니다.
                if (origin != null && isAllowed(origin)) {
                    // 4. 유효한 Origin이라면, 해당 Origin을 명시적으로 허용 목록에 추가합니다.
                    configuration.setAllowedOrigins(List.of(origin));

                    configuration.setAllowedMethods(
                            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    configuration.setAllowedHeaders(List.of("*"));
                    configuration.setAllowCredentials(true);
                }
                // 만약 허용되지 않은 Origin에서 온 요청이라면, allowedOrigins가 비어있게 되고
                // 브라우저는 CORS 에러를 발생시킵니다. (이것이 정상 동작입니다)

                return configuration;
            }
        };
    }

    private boolean isAllowed(String origin) {
        try {
            URI originUri = new URI(origin);
            String requestHost = originUri.getHost();

            // 허용된 호스트 목록에 요청의 호스트가 포함되어 있는지 확인합니다.
            return allowedHosts.contains(requestHost);
        } catch (URISyntaxException e) {
            // 유효하지 않은 Origin 형식이면 거부합니다.
            return false;
        }
    }
}
