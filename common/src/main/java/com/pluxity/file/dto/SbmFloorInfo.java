package com.pluxity.file.dto;

import lombok.Builder;

@Builder
public record SbmFloorInfo(
        String name,
        String groupId
) {}
