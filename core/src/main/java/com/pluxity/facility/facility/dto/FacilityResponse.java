package com.pluxity.facility.facility.dto;

// Removed imports for FileResponse, BaseResponse, Facility for this simplified version
// import com.pluxity.file.dto.FileResponse;
// import com.pluxity.global.response.BaseResponse;
// import com.pluxity.facility.facility.Facility;
// import com.fasterxml.jackson.annotation.JsonUnwrapped;
// import lombok.Builder; // Not using builder for simple record

// @Builder // Removing builder as it's a simple record now
public record FacilityResponse(
    Long id,
    String code,
    String name,
    String description,
    Long drawingFileId, // Simplified from FileResponse drawing
    Long thumbnailFileId  // Simplified from FileResponse thumbnail
    // @JsonUnwrapped BaseResponse baseResponse // Removed for simplicity
) {
    // Static from() method removed, will be handled by MapStruct mapper
}
