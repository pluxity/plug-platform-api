package com.pluxity.facility.floor.dto;

import com.pluxity.facility.floor.Floor;

public record FloorResponse(String name, Integer groupId) {
    public static FloorResponse from(Floor floor) {
        return new FloorResponse(floor.getName(), floor.getGroupId());
    }
}
