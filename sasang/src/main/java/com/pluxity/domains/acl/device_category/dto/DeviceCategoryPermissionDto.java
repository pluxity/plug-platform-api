package com.pluxity.domains.acl.device_category.dto;

import java.util.List;

public record DeviceCategoryPermissionDto(
        Long deviceCategoryId,
        String deviceCategoryName,
        List<String> grantedUsers,
        List<String> grantedRoles) {}
