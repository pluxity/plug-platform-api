package com.pluxity.device.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
    gs_device("DEVICE"),
    climate("CLIMATE");

    private final String type;

    public static List<String> getTypeList() {
        return Arrays.stream(DeviceType.values()).map(DeviceType::getType).collect(Collectors.toList());
    }
}
