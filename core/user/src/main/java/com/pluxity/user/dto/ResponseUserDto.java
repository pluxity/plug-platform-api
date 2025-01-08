package com.pluxity.user.dto;

import com.pluxity.user.constant.Role;
import lombok.Builder;

@Builder
public record ResponseUserDto(String username, String name, String code, Role role) {}
