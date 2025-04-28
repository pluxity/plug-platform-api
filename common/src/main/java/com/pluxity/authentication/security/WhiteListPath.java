package com.pluxity.authentication.security;

import lombok.Getter;

@Getter
public enum WhiteListPath {
    AUTH("auth"),
    ACTUATOR("actuator"),
    HEALTH("health"),
    INFO("info"),
    PROMETHEUS("prometheus"),
    APIDOC("api-docs"),
    SWAGGER("swagger-ui"),
    FACILITY("facilities"),
    FILES("files");

    private final String path;

    WhiteListPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
