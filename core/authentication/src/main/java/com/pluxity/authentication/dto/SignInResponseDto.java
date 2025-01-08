package com.pluxity.authentication.dto;

import lombok.Builder;

@Builder
public record SignInResponseDto(
        String accessToken, String refreshToken, String name, String code) {}
