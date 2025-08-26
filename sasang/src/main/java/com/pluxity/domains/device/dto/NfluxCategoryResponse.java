package com.pluxity.domains.device.dto;

import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.file.dto.FileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record NfluxCategoryResponse(
        Long id,
        String name,
        Long parentId,
        String contextPath,
        @Schema(description = "자식 카테고리 목록") List<NfluxCategoryResponse> children,
        @Schema(description = "아이콘 파일 정보") FileResponse iconFile,
        @Schema(description = "카테고리 깊이", example = "1") int depth) {

    public static NfluxCategoryResponse from(NfluxCategory nfluxCategory) {
        return new NfluxCategoryResponse(
                nfluxCategory.getId(),
                nfluxCategory.getName(),
                nfluxCategory.getParent() != null ? nfluxCategory.getParent().getId() : null,
                nfluxCategory.getContextPath(),
                nfluxCategory.getChildren().stream()
                        .map(e -> NfluxCategoryResponse.from((NfluxCategory) e))
                        .collect(Collectors.toList()),
                null,
                nfluxCategory.getDepth());
    }

    public static NfluxCategoryResponse from(NfluxCategory nfluxCategory, FileResponse iconFile) {
        return new NfluxCategoryResponse(
                nfluxCategory.getId(),
                nfluxCategory.getName(),
                nfluxCategory.getParent() != null ? nfluxCategory.getParent().getId() : null,
                nfluxCategory.getContextPath(),
                new ArrayList<>(),
                iconFile != null ? iconFile : FileResponse.empty(),
                nfluxCategory.getDepth());
    }

    public static NfluxCategoryResponse fromWithoutChildren(
            NfluxCategory nfluxCategory, FileResponse iconFile) {
        return new NfluxCategoryResponse(
                nfluxCategory.getId(),
                nfluxCategory.getName(),
                nfluxCategory.getParent() != null ? nfluxCategory.getParent().getId() : null,
                nfluxCategory.getContextPath(),
                List.of(), // 자식 목록을 빈 리스트로 설정
                iconFile,
                nfluxCategory.getDepth());
    }
}
