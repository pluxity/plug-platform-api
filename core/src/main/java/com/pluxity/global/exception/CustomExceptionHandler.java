package com.pluxity.global.exception;

import com.pluxity.global.response.ErrorResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseBody> handleCustomException(CustomException e) {

        LOGGER.error("CustomException", e);
        var errorResponseBody =
                ErrorResponseBody.of(e.getErrorCode().getHttpStatus(), e.getErrorCode().getMessage());

        return new ResponseEntity<>(errorResponseBody, e.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseBody> handleException(Exception e) {

        LOGGER.error("NoResourceFoundException", e);
        return new ResponseEntity<>(
                ErrorResponseBody.of(HttpStatus.NOT_FOUND, "해당 경로를 찾지 못했습니다. url 을 확인해주세요"),
                HttpStatus.NOT_FOUND);
    }
}
