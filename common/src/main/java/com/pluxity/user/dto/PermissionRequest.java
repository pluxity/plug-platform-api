package com.pluxity.user.dto;

import com.pluxity.user.entity.ResourceType;
import java.util.List;

public record PermissionRequest(Long roleId, ResourceType resourceName, List<Long> resourceId) {}
