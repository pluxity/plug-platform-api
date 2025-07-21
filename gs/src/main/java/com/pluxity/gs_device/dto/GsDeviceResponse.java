package com.pluxity.gs_device.dto;

import com.pluxity.device.dto.DeviceCategoryResponse;
import com.pluxity.feature.dto.FeatureResponse;
import lombok.Builder;

@Builder
public record GsDeviceResponse(
        String id, String name, FeatureResponse feature, DeviceCategoryResponse deviceCategory) {}
