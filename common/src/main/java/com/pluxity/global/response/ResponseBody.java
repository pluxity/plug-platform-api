package com.pluxity.global.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseBody {

    private final String timestamp;
    private final int status;
    private final String message;

    public ResponseBody(HttpStatus status, String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        this.timestamp = now.format(formatter);
        this.status = status.value();
        this.message = message;
    }
}
