package com.pluxity.station.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record StationInfoResponse(List<Long> lineIds, List<String> stationCodes) {}
