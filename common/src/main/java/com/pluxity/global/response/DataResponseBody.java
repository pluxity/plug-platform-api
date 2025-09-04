package com.pluxity.global.response;

import static com.pluxity.global.constant.SuccessCode.SUCCESS;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataResponseBody<T> extends ResponseBody {

    private final T data;

    public DataResponseBody(HttpStatus status, String message, T data) {
        super(status, message);
        this.data = data;
    }

    public static <T> DataResponseBody<T> of(T data) {
        return new DataResponseBody<>(SUCCESS.getHttpStatus(), SUCCESS.getMessage(), data);
    }
}
