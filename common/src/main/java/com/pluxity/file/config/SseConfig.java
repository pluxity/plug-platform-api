package com.pluxity.file.config;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Configuration
public class SseConfig implements WebMvcConfigurer {
    /** 모든 비동기 요청에 대한 기본 타임아웃을 설정합니다. 이는 SSE가 아닌 다른 비동기 요청에 대한 안전장치 역할을 합니다. */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(30_000L); // 30 seconds
    }

    /** 커스텀 인터셉터를 등록합니다. 이 인터셉터는 SseEmitter를 반환하는 요청을 식별하여 타임아웃을 재설정합니다. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SseTimeoutSettingInterceptor());
    }

    /** SseEmitter를 반환하는 컨트롤러 메소드에 대해 선택적으로 비동기 요청 타임아웃을 재설정하는 인터셉터입니다. */
    static class SseTimeoutSettingInterceptor implements AsyncHandlerInterceptor {

        // 1시간 타임아웃 (밀리초 단위)
        private static final long SSE_TIMEOUT = 60 * 60 * 1000L;

        /**
         * 컨트롤러 메소드가 실행되기 전에 호출됩니다.
         *
         * @param handler 처리될 엔드포인트의 핸들러 (컨트롤러 메소드 정보 포함)
         * @return 요청 처리를 계속하려면 true, 중단하려면 false
         */
        @Override
        public boolean preHandle(
                @Nonnull HttpServletRequest request,
                @Nonnull HttpServletResponse response,
                @Nonnull Object handler) {
            // 핸들러가 실제 컨트롤러 메소드를 나타내는 HandlerMethod 인스턴스인지 확인합니다.
            if (handler instanceof HandlerMethod handlerMethod) {
                // 해당 컨트롤러 메소드의 반환 타입이 SseEmitter 클래스이거나 그 자식 클래스인지 확인합니다.
                if (SseEmitter.class.isAssignableFrom(handlerMethod.getReturnType().getParameterType())) {
                    // 조건이 일치하면, 이 특정 요청에 대한 서블릿 컨테이너의 비동기 타임아웃을
                    // SSE를 위한 긴 타임아웃 값으로 덮어씁니다.
                    request.getAsyncContext().setTimeout(SSE_TIMEOUT);
                }
            }
            // 모든 경우에 요청 처리를 계속 진행하도록 true를 반환합니다.
            return true;
        }
    }
}
