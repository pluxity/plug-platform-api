package com.pluxity.domains.device.service;

import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.entity.DeviceCategory;
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
        DeviceCategory parent = null;
        String name = request.name();

        // 1. DeviceCategoryRequest가 제공된 경우 이를 사용
        if (request.deviceCategoryRequest() != null) {
            DeviceCategoryRequest deviceRequest = request.deviceCategoryRequest();
            // DeviceCategoryRequest의 이름을 우선 사용
            if (deviceRequest.getName() != null) {
                name = deviceRequest.getName();
            }

            // 부모 카테고리 처리
            if (deviceRequest.getParentId() != null) {
                parent = deviceCategoryService.findById(deviceRequest.getParentId());
                if (!(parent instanceof NfluxCategory)) {
                    throw new CustomException(
                            ErrorCode.PERMISSION_DENIED, "부모 카테고리는 NfluxCategory 타입이어야 합니다.");
                }
            }
        }
        // 2. parentId가 직접 제공된 경우
        else if (request.parentId() != null) {
            parent = deviceCategoryService.findById(request.parentId());
            if (!(parent instanceof NfluxCategory)) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED, "부모 카테고리는 NfluxCategory 타입이어야 합니다.");
            }
        }

        // 3. NfluxCategory 생성 및 저장
        NfluxCategory category =
                NfluxCategory.nfluxBuilder()
                        .name(name)
                        .parent((NfluxCategory) parent)
                        .contextPath(request.contextPath())
                        .build();

        // 4. iconFileId 설정 (DeviceCategoryRequest에서 제공된 경우)
        if (request.deviceCategoryRequest() != null
                && request.deviceCategoryRequest().getIconFileId() != null) {
            category.updateIconFileId(request.deviceCategoryRequest().getIconFileId());
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

        // 1. 기본 필드 업데이트
        if (request.name() != null) {
            category.setName(request.name());
        }

        if (request.contextPath() != null) {
            category.updateContextPath(request.contextPath());
        }

        // 2. DeviceCategoryRequest에서 필드 업데이트
        if (request.deviceCategoryRequest() != null) {
            DeviceCategoryRequest deviceRequest = request.deviceCategoryRequest();

            // 이름 업데이트 (request.name()이 없는 경우에만)
            if (request.name() == null && deviceRequest.getName() != null) {
                category.setName(deviceRequest.getName());
            }

            // iconFileId 업데이트
            if (deviceRequest.getIconFileId() != null) {
                category.updateIconFileId(deviceRequest.getIconFileId());
            }

            // 부모 카테고리 업데이트 (request.parentId()가 없는 경우에만)
            if (request.parentId() == null && deviceRequest.getParentId() != null) {
                DeviceCategory parent = deviceCategoryService.findById(deviceRequest.getParentId());
                if (!(parent instanceof NfluxCategory)) {
                    throw new CustomException(
                            ErrorCode.PERMISSION_DENIED, "부모 카테고리는 NfluxCategory 타입이어야 합니다.");
                }
                category.assignToParent(parent);
            }
        }

        // 3. 직접적인 parentId 업데이트 (deviceRequest보다 우선 적용)
        if (request.parentId() != null) {
            DeviceCategory parent = deviceCategoryService.findById(request.parentId());
            if (!(parent instanceof NfluxCategory)) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED, "부모 카테고리는 NfluxCategory 타입이어야 합니다.");
            }
            category.assignToParent(parent);
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
                category.getParent() != null ? category.getParent().getId() : null,
                category.getContextPath(),
                iconFileResponse,
                category.getChildren().stream()
                        .filter(c -> c instanceof NfluxCategory)
                        .map(c -> toResponse((NfluxCategory) c))
                        .collect(Collectors.toList()),
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
