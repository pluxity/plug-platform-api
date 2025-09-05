package com.pluxity.global.constant

import org.springframework.http.HttpStatus

enum class SuccessCode(
    private val httpStatus: HttpStatus,
    private val message: String,
) : Code {
    SUCCESS(HttpStatus.OK, "성공"),
    ;

    override fun getHttpStatus(): HttpStatus = this.httpStatus

    override fun getMessage(): String = this.message

    override fun getStatusName(): String = this.httpStatus.name
}
