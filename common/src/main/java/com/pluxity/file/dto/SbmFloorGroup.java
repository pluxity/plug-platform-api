package com.pluxity.file.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SbmFloorGroup(
        String groupId,
        SbmFloorInfo mainFloorInfo,
        List<SbmFloorInfo> floorInfoList
) {
}
