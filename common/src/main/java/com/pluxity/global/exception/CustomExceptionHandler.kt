package com.pluxity.global.exception

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.response.ErrorResponseBody
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityNotFoundException
import lombok.extern.slf4j.Slf4j
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

private val log = KotlinLogging.logger {}

@Slf4j
@RestControllerAdvice
class CustomExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception) =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponseBody.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."))
            .also { log.error(e) { "Unhandled Exception" } }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException) =
        ResponseEntity
            .status(e.errorCode.getHttpStatus())
            .body(ErrorResponseBody.of(e.errorCode, e.message))
            .also { log.error(e) { "CustomException: ${e.message}" } }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException) =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponseBody.of(HttpStatus.NOT_FOUND, e.message))
            .also { log.error(e) { "EntityNotFoundException" } }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException) =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponseBody.of(HttpStatus.NOT_FOUND, "해당 경로를 찾지 못했습니다. url 을 확인해주세요"))
            .also { log.error(e) { "NoResourceFoundException" } }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponseBody> {
        log.error(e) { "Validation Error" }

        val fieldErrors = e.bindingResult.fieldErrors
        val errorMessage =
            fieldErrors.joinToString { error: FieldError -> "$error.field: $error.defaultMessage" }

        return ResponseEntity<ErrorResponseBody>(
            ErrorResponseBody.of(HttpStatus.BAD_REQUEST, errorMessage),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponseBody> =
        ResponseEntity<ErrorResponseBody>(
            ErrorResponseBody.of(
                HttpStatus.BAD_REQUEST, // HTTP 상태 코드
                e.message
                    ?.takeIf { "Required request body is missing" in it }
                    ?.let { "필수 요청 본문(Request Body)이 누락되었습니다." }
                    ?: "필수 요청 본문이 누락되었거나 형식이 잘못되었습니다.", // 클라이언트에게 보여줄 메시지
            ),
            HttpStatus.BAD_REQUEST,
        ).also { log.error(e) { "HttpMessageNotReadableException: ${e.message}" } }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException) =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseBody.of(HttpStatus.BAD_REQUEST, "필수 요청 파라미터(${e.parameterName})가 누락되었습니다."))
            .also { log.error(e) { "handleMissingServletRequestParameterException: ${e.message}" } }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(e: DataIntegrityViolationException) =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseBody.of(ErrorCode.DUPLICATE_RESOURCE_ID))
            .also { log.error(e) { "handleDataIntegrityViolationException: ${e.message}" } }
}
