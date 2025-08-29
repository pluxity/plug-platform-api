package com.pluxity.authentication.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class SignInRequest(
    @field:NotNull(message = "사용자 ID는 필수 입니다.")
    @field:NotBlank(message = "사용자 ID는 공백이 될 수 없습니다.")
    @field:Size(max = 20, message = "사용자 ID는 20자 이하 여야 합니다.")
    val username: String,
    @field:NotNull(message = "비밀번호는 필수 입니다.")
    @field:NotBlank(message = "비밀번호는 공백이 될 수 없습니다.")
    @field:Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하 여야 합니다.")
    val password: String,
)
