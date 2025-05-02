package com.pluxity.facility.dto;

import com.pluxity.facility.entity.Floor;

public record FloorResponse(
        String name,
        Integer groupId
) {
    public static FloorResponse from(Floor floor) {
        return new FloorResponse(floor.getName(), floor.getGroupId());
    }
}
