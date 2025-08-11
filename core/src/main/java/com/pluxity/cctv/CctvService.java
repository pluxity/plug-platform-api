package com.pluxity.cctv;

import com.pluxity.cctv.dto.CctvCreateRequest;
import com.pluxity.cctv.dto.CctvResponse;
import com.pluxity.cctv.dto.CctvUpdateRequest;
import com.pluxity.cctv.entity.Cctv;
import com.pluxity.cctv.repository.CctvRepository;
import com.pluxity.cctv.repository.DeviceCctvRepository;
import com.pluxity.device.dto.DeviceCategoryResponseWithoutChildren;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CctvService {

    private final CctvRepository cctvRepository;
    private final DeviceCategoryService deviceCategoryService;
    private final FileService fileService;
    private final DeviceCctvRepository deviceCctvRepository;

    @Transactional
    public String create(@Valid CctvCreateRequest request) {
        DeviceCategory category =
                MappingUtils.findByIdIfExists(request.categoryId(), deviceCategoryService::findById);

        return cctvRepository
                .save(
                        Cctv.builder()
                                .id(request.id())
                                .name(request.name())
                                .url(request.url())
                                .category(category)
                                .build())
                .getId();
    }

    @Transactional(readOnly = true)
    public List<CctvResponse> findAll() {
        List<Cctv> list = cctvRepository.findAll();
        return list.stream().map(this::createResponse).toList();
    }

    @Transactional(readOnly = true)
    public CctvResponse getById(String id) {
        return createResponse(findById(id));
    }

    private CctvResponse createResponse(Cctv cctv) {
        FileResponse thumbnailFile = getThumbnailFile(cctv);
        return CctvResponse.builder()
                .id(cctv.getId())
                .name(cctv.getName())
                .url(cctv.getUrl())
                .feature(cctv.getFeature() != null ? FeatureResponse.from(cctv.getFeature()) : null)
                .deviceCategory(
                        cctv.getCategory() != null
                                ? DeviceCategoryResponseWithoutChildren.from(cctv.getCategory(), thumbnailFile)
                                : null)
                .build();
    }

    @Transactional
    public void update(String id, CctvUpdateRequest request) {
        Cctv cctv = findById(id);
        cctv.updateCctv(request);
        DeviceCategory category =
                MappingUtils.findByIdIfExists(request.categoryId(), deviceCategoryService::findById);
        cctv.changeCategory(category);
    }

    @Transactional
    public void delete(String id) {
        Cctv cctv = findById(id);
        deviceCctvRepository.deleteByCctvIdIn(List.of(cctv.getId()));
        cctvRepository.delete(cctv);
    }

    @Transactional(readOnly = true)
    public Cctv findById(String id) {
        return cctvRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CCTV, id));
    }

    private FileResponse getThumbnailFile(Cctv cctv) {
        return Optional.ofNullable(cctv.getCategory())
                .map(DeviceCategory::getIconFileId)
                .map(fileService::getFileResponse)
                .orElse(null);
    }
}
