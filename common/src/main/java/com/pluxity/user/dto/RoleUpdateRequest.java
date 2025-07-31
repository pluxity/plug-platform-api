package com.pluxity.user.dto;

import java.util.List;

public record RoleUpdateRequest(String name, String description, List<Long> permissionGroupIds) {}
