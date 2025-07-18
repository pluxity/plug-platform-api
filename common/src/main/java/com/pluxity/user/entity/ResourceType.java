package com.pluxity.user.entity;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResourceType {
    FACILITY("Facility"),
    FACILITY_CATEGORY("FacilityCategory"),
    BUILDING("Building");

    private final String resourceName;

    public static ResourceType fromString(String resourceName) {
        return Arrays.stream(ResourceType.values())
                .filter(type -> type.resourceName.equalsIgnoreCase(resourceName))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_RESOURCE_TYPE, resourceName));
    }
}
