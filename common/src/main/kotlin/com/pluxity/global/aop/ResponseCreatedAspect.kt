package com.pluxity.global.aop

import com.pluxity.global.annotation.ResponseCreated
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.net.URI

@Aspect
@Component
class ResponseCreatedAspect {
    @Around("@annotation(responseCreated)")
    fun handleResponseCreated(
        joinPoint: ProceedingJoinPoint,
        responseCreated: ResponseCreated,
    ): ResponseEntity<Void> {
        val result = joinPoint.proceed() as ResponseEntity<*>
        val id = result.getBody()
        return ResponseEntity.created(URI.create(responseCreated.path.replace("{id}", id?.toString() ?: ""))).build()
    }
}
