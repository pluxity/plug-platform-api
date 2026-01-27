package com.pluxity.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserCreateRequest(
    @field:NotBlank(message = "사용자 ID는 공백이 될 수 없습니다.")
    @field:Size(max = 20, message = "사용자 ID는 20자 이하 여야 합니다.")
    val username: String,
    @field:NotBlank(message = "비밀번호는 공백이 될 수 없습니다.")
    @field:Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하 여야 합니다.")
    var password: String,
    @field:NotBlank(message = "이름은 공백이 될 수 없습니다.")
    @field:Size(max = 10, message = "이름은 10자 이하 여야 합니다.")
    val name: String,
    @field:Size(max = 20, message = "코드는 20자 이하 여야 합니다.")
    val code: String? = null,
    @field:Size(max = 20, message = "연락처는 20자 이하 여야 합니다.")
    val phoneNumber: String? = null,
    @field:Size(max = 50, message = "부서는 50자 이하 여야 합니다.")
    val department: String? = null,
    val roleIds: List<Long> = listOf(),
)
