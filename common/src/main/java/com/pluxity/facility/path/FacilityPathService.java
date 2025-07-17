package com.pluxity.facility.path;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_FACILITY_PATH;

import com.pluxity.facility.Facility;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FacilityPathService {
    private final FacilityPathRepository facilityPathRepository;

    @Transactional(readOnly = true)
    public FacilityPath findById(Long id) {
        return facilityPathRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY_PATH, id));
    }

    @Transactional
    public void update(Long pathId, String name, String type, String path) {
        FacilityPath facilityPath = findById(pathId);
        if (StringUtils.hasText(name)) {
            facilityPath.updateName(name);
        }
        if (StringUtils.hasText(type)) {
            facilityPath.updatePathType(PathType.from(type));
        }
        if (StringUtils.hasText(path)) {
            facilityPath.updatePath(path);
        }
    }

    @Transactional
    public void save(Facility facility, String name, String type, String path) {
        facilityPathRepository.save(
                FacilityPath.builder()
                        .facility(facility)
                        .name(name)
                        .pathType(PathType.valueOf(type))
                        .path(path)
                        .build());
    }

    @Transactional
    public void delete(Long pathId) {
        facilityPathRepository.delete(findById(pathId));
    }
}
