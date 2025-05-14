package com.pluxity.file.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record SbmFloorGroup(
        String groupId, SbmFloorInfo mainFloorInfo, List<SbmFloorInfo> floorInfoList) {}
