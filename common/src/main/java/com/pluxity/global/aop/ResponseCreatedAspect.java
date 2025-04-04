package com.pluxity.global.aop;

import com.pluxity.global.annotation.ResponseCreated;
import java.net.URI;

import com.pluxity.global.response.CreatedResponseBody;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Aspect
@Component
public class ResponseCreatedAspect {

    @Around("@annotation(responseCreated)")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> handleResponseCreated(ProceedingJoinPoint joinPoint, ResponseCreated responseCreated) throws Throwable {
        ResponseEntity<?> result = (ResponseEntity<?>) joinPoint.proceed();
        Object body = result.getBody();

        if (body instanceof CreatedResponseBody) {

            Object newId = ((CreatedResponseBody<Long>) body).getId();
            URI location;

            if ("/{id}".equals(responseCreated.path())) {
                location = ServletUriComponentsBuilder.fromCurrentRequest()
                        .path(responseCreated.path())
                        .buildAndExpand(newId)
                        .toUri();
            } else {
                location = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(responseCreated.path())
                        .buildAndExpand(newId)
                        .toUri();
            }
            return ResponseEntity.created(location).body(body);
        }
        return result;
    }
}
