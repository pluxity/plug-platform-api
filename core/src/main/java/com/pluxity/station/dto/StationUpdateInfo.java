package com.pluxity.station.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record StationUpdateInfo(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Long> lineIds,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<String> stationCodes) {}
