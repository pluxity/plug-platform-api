package com.pluxity.global.aop;

import com.pluxity.global.annotation.ResponseCreated;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Aspect
@Component
public class ResponseCreatedAspect {

    @Around("@annotation(responseCreated)")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Long> handleResponseCreated(
            ProceedingJoinPoint joinPoint, ResponseCreated responseCreated) throws Throwable {
        ResponseEntity<Long> result = (ResponseEntity<Long>) joinPoint.proceed();

        if (result.getBody() instanceof Long id) {
            URI location =
                    ServletUriComponentsBuilder.fromCurrentRequest()
                            .path(responseCreated.path())
                            .buildAndExpand(id)
                            .toUri();

            return ResponseEntity.created(location).body(id);
        }

        return result;
    }
}
