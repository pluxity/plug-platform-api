package com.pluxity.facility.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class FacilityCreateRequest(
    @field:Schema(description = "시설 이름", example = "서울역", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "이름은 필수 입니다.")
    @field:Size(max = 50, message = "이름은 최대 50자까지 입력 가능합니다.")
    val name: String,
    @field:Schema(
        description = "시설 코드",
        example = "SEOUL_STATION",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:NotBlank(message = "코드는 필수 입니다.")
    @field:Size(max = 50, message = "코드는 최대 50자까지 입력 가능합니다.")
    val code: String,
    @field:Schema(description = "시설 설명", example = "description")
    @field:Size(max = 255, message = "시설 설명은 최대 255자까지 입력 가능합니다.")
    val description: String? = null,
    @field:Schema(description = "도면 파일 ID", example = "1")
    val drawingFileId: Long? = null,
    @field:Schema(description = "썸네일파일 ID", example = "1")
    val thumbnailFileId: Long? = null,
    @field:Schema(description = "경도", example = "127")
    val lon: Double? = null,
    @field:Schema(description = "위도", example = "37")
    val lat: Double? = null,
    @field:Schema(description = "위치 관련 부가정보", example = "[{\"height\":..}]]")
    val locationMeta: String? = null,
)
