package com.pluxity.authentication.dto;

import lombok.Builder;

@Builder
public record SignInResponse(
        String accessToken, String refreshToken, String name, String code) {}
