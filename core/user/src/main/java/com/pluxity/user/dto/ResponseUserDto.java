package com.pluxity.user.dto;

import com.pluxity.user.entity.Role;
import java.util.List;
import lombok.Builder;

@Builder
public record ResponseUserDto(String username, String name, String code, List<Role> roles) {}
