package com.pluxity.authentication.security

enum class WhiteListPath(
    val path: String,
) {
    AUTH_IN("auth/sign-in"),
    AUTH_UP("auth/sign-up"),
    REFRESH_TOKEN("auth/refresh-token"),
    ACTUATOR("actuator"),
    APIDOC("api-docs"),
    HEALTH("health"),
    INFO("info"),
    PROMETHEUS("prometheus"),
    SWAGGER("swagger-ui"),
}
