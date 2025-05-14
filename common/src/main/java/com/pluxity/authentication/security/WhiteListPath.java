package com.pluxity.authentication.security;

import lombok.Getter;

@Getter
public enum WhiteListPath {
    AUTH("auth"),
    ACTUATOR("actuator"),
    APIDOC("api-docs"),
    HEALTH("health"),
    INFO("info"),
    PROMETHEUS("prometheus"),
    SWAGGER("swagger-ui"),
    ;

    private final String path;

    WhiteListPath(String path) {
        this.path = path;
    }
}
