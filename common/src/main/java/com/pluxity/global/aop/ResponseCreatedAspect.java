package com.pluxity.global.aop;

import com.pluxity.global.annotation.ResponseCreated;
import java.net.URI;

import com.pluxity.global.response.CreatedResponseBody;
import com.pluxity.global.response.ResponseBody;
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
    public ResponseEntity<?> handleResponseCreated(ProceedingJoinPoint joinPoint, ResponseCreated responseCreated) throws Throwable {
        ResponseEntity<?> result = (ResponseEntity<?>) joinPoint.proceed();
        Object body = result.getBody();

        if (body instanceof CreatedResponseBody) {
            Long newId = ((CreatedResponseBody) body).getId();
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path(responseCreated.path())
                    .buildAndExpand(newId)
                    .toUri();
            return ResponseEntity.created(location).body(body);
        }
        return result;
    }
}
