package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserCreateRequest(
        @NotNull(message = "사용자 ID는 필수 입니다.")
        @NotBlank(message = "사용자 ID는 공백이 될 수 없습니다.")
        @Size(max = 20, message = "사용자 ID는 20자 이하 여야 합니다.")
        String username,
        @NotNull(message = "비밀번호는 필수 입니다.")
        @NotBlank(message = "비밀번호는 공백이 될 수 없습니다.")
        @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하 여야 합니다.")
        String password,
        @NotNull(message = "이름은 필수 입니다.")
        @NotBlank(message = "이름은 공백이 될 수 없습니다.")
        @Size(max = 10, message = "이름은 10자 이하 여야 합니다.")
        String name,
        @NotNull(message = "코드는 필수 입니다.")
        @NotBlank(message = "코드는 공백이 될 수 없습니다.")
        @Size(max = 20, message = "코드는 20자 이하 여야 합니다.")
        String code
) {

}
