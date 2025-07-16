package com.pluxity.facility.dto;

import com.pluxity.facility.history.FacilityHistory;
import com.pluxity.file.dto.FileResponse;

public record FacilityHistoryResponse(
        Long id, String comment, String createdAt, String createdBy, FileResponse file) {
    public static FacilityHistoryResponse from(FacilityHistory entity, FileResponse file) {
        return new FacilityHistoryResponse(
                entity.getId(),
                entity.getComment(),
                entity.getCreatedAt().toString(),
                entity.getCreatedBy(),
                file);
    }
}
