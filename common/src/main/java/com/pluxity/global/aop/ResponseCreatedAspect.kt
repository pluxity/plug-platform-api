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
    @Throws(Throwable::class)
    fun <ID> handleResponseCreated(
        joinPoint: ProceedingJoinPoint,
        responseCreated: ResponseCreated,
    ): ResponseEntity<ID> {
        val result = joinPoint.proceed() as ResponseEntity<*>
        val id = result.getBody()

        if (id == null) {
            val location = URI.create(responseCreated.path)
            return ResponseEntity.created(location).build<ID>()
        }

        val location = URI.create(responseCreated.path.replace("{id}", id.toString()))
        return ResponseEntity.created(location).build<ID>()
    }
}
