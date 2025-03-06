package com.pluxity.global.response;

import static com.pluxity.global.constant.SuccessCode.SUCCESS;

import com.pluxity.global.constant.SuccessCode;
import jakarta.validation.constraints.NotNull;
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

    public static <T> DataResponseBody<T> of(HttpStatus status, String message, T data) {
        return new DataResponseBody<>(status, message, data);
    }

    public static <C extends SuccessCode, T> DataResponseBody<T> of(@NotNull C code, T data) {
        return new DataResponseBody<>(code.getHttpStatus(), code.getMessage(), data);
    }
}
