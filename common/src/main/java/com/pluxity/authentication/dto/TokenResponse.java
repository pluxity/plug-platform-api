package com.pluxity.authentication.dto;

import lombok.Builder;

@Builder
public record TokenResponse(String accessToken) {}
