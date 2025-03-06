package com.pluxity.user.dto;

import lombok.Builder;

@Builder
public record UserUpdateRequest(String username, String name, String code) {}
