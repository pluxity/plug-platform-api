package com.pluxity.permission.dto;

import java.util.List;

public record PermissionResponse(String resourceType, List<String> resourceIds) {}
