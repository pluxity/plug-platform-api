package com.pluxity.domains.device.service;

import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.domains.device.dto.NfluxCategoryCreateRequest;
import com.pluxity.domains.device.dto.NfluxCategoryResponse;
import com.pluxity.domains.device.dto.NfluxCategoryUpdateRequest;
import com.pluxity.domains.device.dto.NfluxResponse;
import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.domains.device.repository.NfluxCategoryRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NfluxCategoryService {

    private final NfluxCategoryRepository nfluxCategoryRepository;
    private final DeviceCategoryService deviceCategoryService;
    private final FileService fileService;
    private final NfluxService nfluxService;

    @Transactional
    public Long save(NfluxCategoryCreateRequest request) {
        String name = request.name();

        // NfluxCategory 생성 및 저장
        NfluxCategory category =
                NfluxCategory.nfluxBuilder()
                        .name(name)
                        .parent(null) // MaxDepth가 1이므로 부모 카테고리는 null
                        .contextPath(request.contextPath())
                        .build();

        // iconFileId 설정
        if (request.iconFileId() != null) {
            category.updateIconFileId(request.iconFileId());
        }

        return nfluxCategoryRepository.save(category).getId();
    }

    public List<NfluxCategoryResponse> findAll() {
        return nfluxCategoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<NfluxCategoryResponse> findAllRoots() {
        return nfluxCategoryRepository.findAllRootCategories().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public NfluxCategoryResponse findById(Long id) {
        NfluxCategory category =
                nfluxCategoryRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        return toResponse(category);
    }

    @Transactional
    public NfluxCategoryResponse update(Long id, NfluxCategoryUpdateRequest request) {
        NfluxCategory category =
                nfluxCategoryRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        // 기본 필드 업데이트
        if (request.name() != null) {
            category.setName(request.name());
        }

        if (request.contextPath() != null) {
            category.updateContextPath(request.contextPath());
        }

        // iconFileId 업데이트
        if (request.iconFileId() != null) {
            category.updateIconFileId(request.iconFileId());
        }

        return toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        NfluxCategory category =
                nfluxCategoryRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        if (!category.getChildren().isEmpty()) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED, "하위 카테고리가 있어 삭제할 수 없습니다.");
        }

        if (!category.getDevices().isEmpty()) {
            throw new CustomException(ErrorCode.CATEGORY_HAS_DEVICES, "연결된 디바이스가 있어 삭제할 수 없습니다.");
        }

        nfluxCategoryRepository.delete(category);
    }

    private NfluxCategoryResponse toResponse(NfluxCategory category) {
        FileResponse iconFileResponse = getIconFileResponse(category);

        return new NfluxCategoryResponse(
                category.getId(),
                category.getName(),
                category.getContextPath(),
                iconFileResponse,
                BaseResponse.of(category));
    }

    private FileResponse getIconFileResponse(NfluxCategory category) {
        if (category.getIconFileId() == null) {
            return FileResponse.empty();
        }

        try {
            return fileService.getFileResponse(category.getIconFileId());
        } catch (Exception e) {
            log.error("Failed to get icon file: {}", e.getMessage());
            return FileResponse.empty();
        }
    }

    @Transactional(readOnly = true)
    public List<NfluxResponse> findDevicesByCategoryId(Long categoryId) {
        // 카테고리가 존재하는지 확인
        NfluxCategory category =
                nfluxCategoryRepository
                        .findById(categoryId)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        // 해당 카테고리에 속한 디바이스들을 조회
        return nfluxService.findByCategoryId(categoryId);
    }
}
