package com.pluxity.device.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCategoryUpdateRequest {

    @Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다.")
    private String name;

    private Long parentId;

    private Long thumbnailFileId;
}
