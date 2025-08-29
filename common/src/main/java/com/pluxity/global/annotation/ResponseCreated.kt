package com.pluxity.global.annotation

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ResponseStatus(HttpStatus.CREATED)
annotation class ResponseCreated(
    val path: String = "/{id}",
)
