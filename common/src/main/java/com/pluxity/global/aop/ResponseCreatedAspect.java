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
    public <ID> ResponseEntity<ID> handleResponseCreated(
            ProceedingJoinPoint joinPoint, ResponseCreated responseCreated) throws Throwable {
        ResponseEntity<ID> result = (ResponseEntity<ID>) joinPoint.proceed();
        ID id = result.getBody();

        URI location;

        if ("/{id}".equals(responseCreated.path())) {
            location =
                    ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path(responseCreated.path())
                            .buildAndExpand(id)
                            .toUri();
        } else {
            location =
                    ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path(responseCreated.path())
                            .buildAndExpand()
                            .toUri();
        }
        return ResponseEntity.created(location).build();
    }
}
