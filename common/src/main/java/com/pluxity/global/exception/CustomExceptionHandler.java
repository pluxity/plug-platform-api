package com.pluxity.global.exception;

import com.pluxity.global.response.ErrorResponseBody;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseBody> handleException(Exception e) {
        LOGGER.error("Unhandled Exception", e);
        return new ResponseEntity<>(
                ErrorResponseBody.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseBody> handleCustomException(CustomException e) {
        LOGGER.error("CustomException", e);

        if (e.getErrorCode() != null) {
            var errorResponseBody =
                    ErrorResponseBody.of(e.getErrorCode().getHttpStatus(), e.getMessage());
            return new ResponseEntity<>(errorResponseBody, e.getErrorCode().getHttpStatus());
        } else {
            var errorResponseBody = ErrorResponseBody.of(e.getHttpStatus(), e.getMessage());
            return new ResponseEntity<>(errorResponseBody, e.getHttpStatus());
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseBody> handleEntityNotFoundException(
            EntityNotFoundException e) {
        LOGGER.error("EntityNotFoundException", e);
        return new ResponseEntity<>(
                ErrorResponseBody.of(HttpStatus.NOT_FOUND, e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseBody> handleNoResourceFoundException(
            NoResourceFoundException e) {
        LOGGER.error("NoResourceFoundException", e);
        return new ResponseEntity<>(
                ErrorResponseBody.of(HttpStatus.NOT_FOUND, "해당 경로를 찾지 못했습니다. url 을 확인해주세요"),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseBody> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e) {
        LOGGER.error("Validation Error", e);

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage =
                fieldErrors.stream()
                        .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                        .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                ErrorResponseBody.of(HttpStatus.BAD_REQUEST, errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseBody> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        LOGGER.error("HttpMessageNotReadableException: {}", ex.getMessage());

        String detailMessage = "필수 요청 본문이 누락되었거나 형식이 잘못되었습니다.";
        if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
            detailMessage = "필수 요청 본문(Request Body)이 누락되었습니다.";
        }

        ErrorResponseBody errorResponseBody =
                ErrorResponseBody.of(
                        HttpStatus.BAD_REQUEST, // HTTP 상태 코드
                        detailMessage // 클라이언트에게 보여줄 메시지
                        );
        return new ResponseEntity<>(errorResponseBody, HttpStatus.BAD_REQUEST);
    }
}
