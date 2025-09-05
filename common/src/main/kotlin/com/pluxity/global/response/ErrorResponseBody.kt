package com.pluxity.global.response

import org.springframework.http.HttpStatus

class ErrorResponseBody(
    status: HttpStatus,
    message: String?,
    val code: String,
    val error: String,
) : ResponseBody(status.value(), message)
