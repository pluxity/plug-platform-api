package com.pluxity.facility.facility.dto;

import com.pluxity.facility.facility.FacilityType;
import com.pluxity.file.dto.FileResponse;
import java.util.Date;

public record FacilityHistoryResponse(
    Number revision,
    Date revisionDate,
    String revisionType,
    Long facilityId,
    FacilityType facilityType,
    String code,
    String name,
    String description,
    FileResponse drawingFile,
    FileResponse thumbnailFile
) {}
