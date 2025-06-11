package com.pluxity.facility.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FacilityUpdateRequest(
        @Schema(description = "시설 이름", example = "서울역", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "이름은 필수 입니다.")
                @Size(max = 50, message = "이름은 최대 50자까지 입력 가능합니다.")
                String name,
        @Schema(
                        description = "시설 코드",
                        example = "SEOUL_STATION",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "코드는 필수 입니다.")
                @Size(max = 20, message = "코드는 최대 10자까지 입력 가능합니다.")
                String code,
        String description,
        Long thumbnailFileId,
        Long drawingFileId) {}
