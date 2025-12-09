package com.pluxity.onboarding.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AdminUserCreateRequest(
    @field:Schema(description = "사용자 이름", example = "admin123", minLength = 3, maxLength = 20)
    @field:NotBlank
    @field:Size(min = 3, max = 20)
    val username: String,
    @field:Schema(description = "비밀번호", example = "SecurePass123!", minLength = 8)
    @field:NotBlank
    @field:Size(min = 8, max = 30)
    val password: String,
    @field:Schema(description = "이름", example = "name")
    @field:NotBlank(message = "이름은 필수 입니다.")
    @field:Size(max = 20, message = "이름은 최대 20자까지 입력 가능합니다.")
    val name: String,
)
