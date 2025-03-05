package com.pluxity.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import static com.pluxity.global.constant.SuccessCode.SUCCESS;
import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CreatedResponseBody<ID> extends ResponseBody {

    @JsonIgnore
    private final ID id;

    public CreatedResponseBody(HttpStatus status, String message, ID id) {
        super(status, message);
        this.id = id;
    }

    public static <T> CreatedResponseBody<T> of(@NotNull T id) {
        return new CreatedResponseBody<>(CREATED, SUCCESS.getMessage(), id);
    }

    public static <T> CreatedResponseBody<T> of(HttpStatus status, String message, @NotNull T id) {
        return new CreatedResponseBody<>(status, message, id);
    }
}
