package com.pluxity.facility.dto;

import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.entity.Line;
import java.util.List;
import java.util.stream.Collectors;

public record LineResponse(Long id, String color, String name, List<Long> stationIds) {
    public static LineResponse from(Line line) {
        return new LineResponse(
                line.getId(),
                line.getColor(),
                line.getName(),
                line.getStations().stream().map(Facility::getId).collect(Collectors.toList()));
    }
}
