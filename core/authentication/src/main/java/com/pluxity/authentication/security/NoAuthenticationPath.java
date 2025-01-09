package com.pluxity.authentication.security;

public enum NoAuthenticationPath {
    AUTH("auth"),
    ACTUATOR("actuator"),
    SWAGGER_UI("swagger-ui"),
    API_DOCS("api-docs");

    private final String path;

    NoAuthenticationPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
