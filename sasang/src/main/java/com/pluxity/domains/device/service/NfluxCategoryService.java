package com.pluxity.domains.device.service;

import static com.pluxity.device.service.DeviceCategoryService.DEVICE_CATEGORIES;
import static com.pluxity.global.constant.ErrorCode.CIRCULAR_REFERENCE_CATEGORY;
import static com.pluxity.global.constant.ErrorCode.INVALID_PARENT_CATEGORY;

import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.domains.device.dto.*;
import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.domains.device.repository.NfluxCategoryRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.utils.MappingUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NfluxCategoryService {

    private final NfluxCategoryRepository nfluxCategoryRepository;
    private final FileService fileService;
    private final NfluxService nfluxService;


    @Transactional
    public Long save(NfluxCategoryCreateRequest request) {

        NfluxCategory parent = null;
        if (request.parentId() != null) {
            parent = findNfluxCategoryById(request.parentId());
        }

        NfluxCategory category =
                NfluxCategory.nfluxBuilder()
                        .parent(parent)
                        .name(request.name())
                        .contextPath(request.contextPath())
                        .build();

        DeviceCategory savedCategory = nfluxCategoryRepository.save(category);

        if (request.iconFileId() != null) {
            category.updateIconFileId(
                    fileService.finalizeUpload(request.iconFileId(), savedCategory.getPrefix()).getId());
        }

        return nfluxCategoryRepository.save(category).getId();
    }

    @Transactional
    public void update(Long id, NfluxCategoryUpdateRequest request) {
        DeviceCategory deviceCategory = findNfluxCategoryById(id);

        if (request.parentId() == null) {
            if (request.name() != null) {
                deviceCategory.updateName(request.name());
            }
            deviceCategory.assignToRootPreservingEntity();
        } else {
            NfluxCategory categoryToUpdate = findNfluxCategoryById(id);
            NfluxCategory newParent =
                    Optional.ofNullable(request.parentId()).map(this::findNfluxCategoryById).orElse(null);

            if (categoryToUpdate.getId().equals(request.parentId())) {
                throw new CustomException(INVALID_PARENT_CATEGORY);
            }

            if (isCircularReference(categoryToUpdate, newParent)) {
                throw new CustomException(CIRCULAR_REFERENCE_CATEGORY);
            }

            categoryToUpdate.updateName(request.name());
            categoryToUpdate.assignToParent(newParent);
        }

        if (request.iconFileId() != null) {
            deviceCategory.updateIconFileId(request.iconFileId());
            fileService.finalizeUpload(
                    request.iconFileId(), DEVICE_CATEGORIES + deviceCategory.getId() + "/");
        }

        deviceCategory.updateIconFileId(request.iconFileId());
    }

    private boolean isCircularReference(DeviceCategory source, DeviceCategory target) {
        while (target != null) {
            if (target.getId().equals(source.getId())) {
                return true;
            }
            target = target.getParent();
        }
        return false;
    }

    @Transactional
    public void delete(Long id) {
        NfluxCategory category = findNfluxCategoryById(id);

        if (!category.getChildren().isEmpty()) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED, "하위 카테고리가 있어 삭제할 수 없습니다.");
        }

        if (!category.getDevices().isEmpty()) {
            log.info("카테고리 [{}] 삭제 전 연결된 디바이스 [{}]개와의 연관관계 제거 시작", id, category.getDevices().size());

            category.clearAllDevices();

            log.info("카테고리 [{}]와 모든 디바이스의 연관관계 제거 완료", id);
        }

        nfluxCategoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<NfluxResponse> findDevicesByCategoryId(Long categoryId) {
        findNfluxCategoryById(categoryId);

        return nfluxService.findByCategoryId(categoryId);
    }

    private NfluxCategory findNfluxCategoryById(Long id) {
        return nfluxCategoryRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
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

    public NfluxCategoryAllResponse getNfluxCategories() {
        List<NfluxCategory> allCategories =
                nfluxCategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "CreatedAt"));

        Map<Long, FileResponse> fileMap =
                MappingUtils.getFileMapByIds(
                        allCategories, nfluxCategory -> Stream.of(nfluxCategory.getIconFileId()), fileService);

        List<NfluxCategoryResponse> list =
                allCategories.stream()
                        .map(
                                category ->
                                        NfluxCategoryResponse.from(category, fileMap.get(category.getIconFileId())))
                        .toList();

        return NfluxCategoryAllResponse.of(
                NfluxCategory.builder().build().getMaxDepth(),
                MappingUtils.makeCategoryTree(
                        list,
                        NfluxCategoryResponse::id,
                        NfluxCategoryResponse::parentId,
                        NfluxCategoryResponse::children));
    }

    public List<NfluxCategoryResponse> getChildDeviceCategories(Long parentId) {
        List<NfluxCategory> childCategories = nfluxCategoryRepository.findByParentId(parentId);
        return childCategories.stream()
                .map(this::createDeviceCategoryResponseWithoutChildren)
                .collect(Collectors.toList());
    }

    private NfluxCategoryResponse createDeviceCategoryResponseWithoutChildren(
            NfluxCategory category) {
        FileResponse iconFile =
                category.getIconFileId() != null
                        ? fileService.getFileResponse(category.getIconFileId())
                        : null;
        return NfluxCategoryResponse.fromWithoutChildren(category, iconFile);
    }
}
