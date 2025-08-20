package com.pluxity.user.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
        String name, String code, String phoneNumber, String department, List<Long> roleIds) {}
