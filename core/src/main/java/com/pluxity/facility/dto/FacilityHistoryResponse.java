package com.pluxity.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.global.response.BaseResponse;

import java.util.Date;

public record FacilityHistoryResponse(
    Long facilityId,
    String facilityType,
    String name,
    String description,
    Long drawingFileId,
    Long thumbnailFileId,
    Date changedAt,
    String revisionType,
    @JsonUnwrapped
    BaseResponse baseResponse
) {
} 