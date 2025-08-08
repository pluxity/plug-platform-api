package com.pluxity.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record GsDeviceCctvUpdateRequest(@Schema(description = "CCTV ID 목록") List<String> cctvIds) {
    public GsDeviceCctvUpdateRequest {
        if (cctvIds == null) {
            cctvIds = List.of();
        }
    }
}
