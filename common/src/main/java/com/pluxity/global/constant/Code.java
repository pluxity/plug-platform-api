package com.pluxity.global.constant;

import org.springframework.http.HttpStatus;

public interface Code {

    HttpStatus getHttpStatus();

    String getMessage();

    String getStatusName();

    Integer getStatusValue();
}
