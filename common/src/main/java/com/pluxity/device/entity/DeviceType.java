package com.pluxity.device.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
    gs_device("DEVICE"),
    climate("CLIMATE");

    private final String type;
}
