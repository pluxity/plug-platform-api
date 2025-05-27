package com.pluxity.facility.facility.dto;

import com.pluxity.file.dto.FileResponse;
import java.util.Date;

public record FacilityHistoryResponse(
        Long facilityId,
        String facilityType,
        String code,
        String name,
        String description,
        FileResponse drawingFile,
        FileResponse thumbnailFile,
        Date changedAt,
        String revisionType) {}
