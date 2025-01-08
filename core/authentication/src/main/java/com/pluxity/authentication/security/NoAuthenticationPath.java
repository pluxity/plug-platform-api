package com.pluxity.authentication.security;

public enum NoAuthenticationPath {
    AUTH("auth"),
    ACTUATOR("actuator"),
    APIDOC("api-docs"),
    SWAGGER("swagger");

    private final String path;

    NoAuthenticationPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
