package com.pluxity.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record FacilityUpdateRequest(
        @Schema(description = "시설 이름", example = "서울역", requiredMode = Schema.RequiredMode.REQUIRED)
                @Size(max = 50, message = "이름은 최대 50자까지 입력 가능합니다.")
                String name,
        @Schema(
                        description = "시설 코드",
                        example = "SEOUL_STATION",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @Size(max = 50, message = "코드는 최대 50자까지 입력 가능합니다.")
                String code,
        @Schema(
                        description = "시설 설명",
                        example = "description",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @Size(max = 255, message = "시설 설명은 최대 255자까지 입력 가능합니다.")
                String description,
        @Schema(description = "썸네일파일 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                Long thumbnailFileId,
        @Schema(description = "경도", example = "127", requiredMode = Schema.RequiredMode.REQUIRED)
                Double lon,
        @Schema(description = "위도", example = "37", requiredMode = Schema.RequiredMode.REQUIRED)
                Double lat,
        @Schema(
                        description = "위치 관련 부가정보",
                        example = "[{\"height\":..}]]",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String locationMeta) {}
