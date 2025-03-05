package com.pluxity.authentication.dto;

import lombok.Builder;

@Builder
public record SignInResponse(
        String accessToken, String name, String code) {}
