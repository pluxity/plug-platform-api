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
        var errorResponseBody =
                ErrorResponseBody.of(e.getErrorCode().getHttpStatus(), e.getErrorCode().getMessage());

        return new ResponseEntity<>(errorResponseBody, e.getErrorCode().getHttpStatus());
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
}
