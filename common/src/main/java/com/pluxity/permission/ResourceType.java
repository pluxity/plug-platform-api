package com.pluxity.permission;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResourceType {
    FACILITY("시설", "facilities"),
    DEVICE_CATEGORY("장비 분류", "device-categories");

    private final String resourceName;
    private final String endpoint;

    public static ResourceType fromString(String resourceName) {
        return Arrays.stream(ResourceType.values())
                .filter(type -> type.resourceName.equalsIgnoreCase(resourceName))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_RESOURCE_TYPE, resourceName));
    }
}
