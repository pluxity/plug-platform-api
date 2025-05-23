package com.pluxity.global.aop;

import com.pluxity.global.annotation.ResponseCreated;
import java.net.URI;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ResponseCreatedAspect {

    @Around("@annotation(responseCreated)")
    @SuppressWarnings("unchecked")
    public <ID> ResponseEntity<ID> handleResponseCreated(
            ProceedingJoinPoint joinPoint, ResponseCreated responseCreated) throws Throwable {
        ResponseEntity<ID> result = (ResponseEntity<ID>) joinPoint.proceed();
        ID id = result.getBody();

        if (id == null) {
            URI location = URI.create(responseCreated.path());
            return ResponseEntity.created(location).build();
        }

        URI location = URI.create(responseCreated.path().replace("{id}", id.toString()));
        return ResponseEntity.created(location).build();
    }
}
