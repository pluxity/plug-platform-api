package com.pluxity.cctv.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CctvUpdateRequest(
    @field:Schema(
        description = "CCTV 이름",
        example = "cctv1",
    )
    @field:NotBlank(message = "CCTV 이름은 필수 입니다.")
    @field:Size(
        max = 50,
        message = "CCTV 이름은 최대 50자까지 입력 가능합니다.",
    )
    val name: String,
    @field:Schema(
        description = "CCTV URL",
        example = "rtsp://example.com/stream",
    )
    @field:Size(max = 1000, message = "CCTV URL은 최대 1000자까지 입력 가능합니다.")
    val url: String?,
)
