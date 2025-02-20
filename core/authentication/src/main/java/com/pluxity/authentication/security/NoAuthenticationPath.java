package com.pluxity.authentication.security;

import lombok.Getter;

@Getter
public enum NoAuthenticationPath {
    AUTH("auth"),
    ACTUATOR("actuator"),
    APIDOC("api-docs");

    private final String path;

    NoAuthenticationPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
