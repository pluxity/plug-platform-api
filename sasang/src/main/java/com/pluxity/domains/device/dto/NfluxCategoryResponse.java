package com.pluxity.domains.device.dto;

import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import java.util.stream.Collectors;

public record NfluxCategoryResponse(
        Long id,
        String name,
        Long parentId,
        String contextPath,
        FileResponse iconFile,
        List<NfluxCategoryResponse> children,
        BaseResponse baseResponse) {
    public static NfluxCategoryResponse from(NfluxCategory category) {
        // 실제로는 FileService를 통해 FileResponse를 가져와야 하지만 현재 정보가 부족하므로 빈 객체 반환
        FileResponse iconFileResponse = category.getIconFileId() != null ? FileResponse.empty() : null;

        return new NfluxCategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getContextPath(),
                iconFileResponse,
                category.getChildren().stream()
                        .filter(c -> c instanceof NfluxCategory)
                        .map(c -> NfluxCategoryResponse.from((NfluxCategory) c))
                        .collect(Collectors.toList()),
                BaseResponse.of(category));
    }
}
