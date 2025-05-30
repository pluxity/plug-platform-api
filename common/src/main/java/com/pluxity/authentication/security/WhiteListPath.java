package com.pluxity.authentication.security;

import lombok.Getter;

@Getter
public enum WhiteListPath {
    AUTH_IN("auth/sign-in"),
    AUTH_UP("auth/sign-up"),
    REFRESH_TOKEN("auth/refresh-token"),
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
