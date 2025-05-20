package com.pluxity.domains.device_category_acl.device.dto;

import java.util.List;

public record DeviceCategoryPermissionDto(
        Long deviceCategoryId,
        String deviceCategoryName,
        List<String> grantedUsers,
        List<String> grantedRoles) {}
