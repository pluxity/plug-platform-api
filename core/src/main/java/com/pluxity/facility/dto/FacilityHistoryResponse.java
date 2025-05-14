package com.pluxity.facility.dto;

import java.util.Date;

public record FacilityHistoryResponse(
        Long facilityId,
        String facilityType,
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId,
        Date changedAt,
        String revisionType) {}
