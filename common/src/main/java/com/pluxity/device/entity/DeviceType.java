package com.pluxity.device.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
    cctv("CCTV"),
    gs_device("DEVICE");

    private final String type;
}
