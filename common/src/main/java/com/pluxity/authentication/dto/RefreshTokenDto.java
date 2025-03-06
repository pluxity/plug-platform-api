package com.pluxity.authentication.dto;

import lombok.Builder;

@Builder
public record RefreshTokenDto(String accessToken, String refreshToken) {}
