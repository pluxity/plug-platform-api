package com.pluxity.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import static com.pluxity.global.constant.SuccessCode.SUCCESS;
import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CreatedResponseBody extends ResponseBody {

    @JsonIgnore
    private final Long id;

    public CreatedResponseBody(HttpStatus status, String message, Long id) {
        super(status, message);
        this.id = id;
    }

    public static CreatedResponseBody of(@NotNull Long id) {
        return new CreatedResponseBody(CREATED, SUCCESS.getMessage(), id);
    }

    public static CreatedResponseBody of(HttpStatus status, String message, @NotNull Long id) {
        return new CreatedResponseBody(status, message, id);
    }
}
