package com.pluxity.file.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Configuration
class SseConfig : WebMvcConfigurer {
    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        // 기본 타임아웃은 여전히 30초로 유지
        configurer.setDefaultTimeout(30000L)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 이 인터셉터는 이제 afterConcurrentHandlingStarted를 사용하므로
        // preHandle에서는 아무 일도 하지 않습니다.
        registry.addInterceptor(SseTimeoutSettingInterceptor())
    }

    /** SseEmitter를 반환하여 비동기 요청 처리가 시작된 후, 해당 요청의 타임아웃을 재설정하는 인터셉터.  */
    internal class SseTimeoutSettingInterceptor : AsyncHandlerInterceptor {
        /** 이 메소드는 컨트롤러가 SseEmitter를 반환하고, 서블릿 스레드가 풀에 반환된 직후(즉, 비동기 처리가 시작된 후)에 호출됩니다.  */
        override fun afterConcurrentHandlingStarted(
            request: HttpServletRequest,
            response: HttpServletResponse,
            handler: Any,
        ) {
            // preHandle에서 했던 것과 동일한 검증 로직 수행
            if (handler is HandlerMethod) {
                if (SseEmitter::class.java.isAssignableFrom(handler.returnType.getParameterType())) {
                    // 이제 isAsyncStarted()가 true인 안전한 시점이므로, getAsyncContext() 호출 가능
                    request.asyncContext.timeout = SSE_TIMEOUT
                }
            }
        }

        companion object {
            private const val SSE_TIMEOUT = 30 * 60 * 1000L // 30분
        }
    }
}
