package com.pluxity.user.dto;

import java.util.List;

public record RolePermissionUpdateRequest(List<Long> permissionIds) {}
