package com.pluxity.user.dto;

import lombok.Builder;

@Builder
public record UserUpdateRequest(String name, String code, String phoneNumber, String department) {}
