package com.pluxity.file.dto;

import lombok.Builder;

@Builder
public record SbmFloorInfo(
        String floorId,
        String floorName,
        String fileName,
        String floorBase,
        String floorGroup,
        String isMain) {}
