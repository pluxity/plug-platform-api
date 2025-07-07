package com.pluxity.facility.line.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.Facility;
import com.pluxity.facility.line.Line;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import java.util.stream.Collectors;

public record LineResponse(
        Long id,
        String color,
        String name,
        List<Long> stationIds,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static LineResponse from(Line line) {
        return new LineResponse(
                line.getId(),
                line.getColor(),
                line.getName(),
                line.getStations().stream().map(Facility::getId).collect(Collectors.toList()),
                BaseResponse.of(line));
    }
}
