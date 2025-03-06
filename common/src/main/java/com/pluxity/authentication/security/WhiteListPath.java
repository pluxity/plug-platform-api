package com.pluxity.authentication.security;

import lombok.Getter;

@Getter
public enum WhiteListPath {
    AUTH("auth"),
    ACTUATOR("actuator"),
    APIDOC("api-docs"),
    FILES("files");

    private final String path;

    WhiteListPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
