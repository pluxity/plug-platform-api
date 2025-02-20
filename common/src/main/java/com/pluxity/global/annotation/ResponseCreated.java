package com.pluxity.global.annotation;

import java.lang.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ResponseStatus(HttpStatus.CREATED)
public @interface ResponseCreated {
    String path() default "/{id}";
}
