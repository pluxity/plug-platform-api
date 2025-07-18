package com.pluxity.facility.history;

import com.pluxity.facility.dto.FacilityHistoryResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.utils.MappingUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacilityHistoryService {

    private final FacilityHistoryRepository facilityHistoryRepository;
    private final FileService fileService;

    @Transactional
    public void save(Long fileId, Long facilityId, String comment) {
        facilityHistoryRepository.save(
                FacilityHistory.builder().fileId(fileId).facilityId(facilityId).comment(comment).build());
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findByFacilityId(Long facilityId) {
        List<FacilityHistory> histories =
                facilityHistoryRepository.findByFacilityIdOrderByCreatedAtDesc(facilityId);
        Map<Long, FileResponse> fileMap =
                MappingUtils.getFileMapByIds(histories, v -> Stream.of(v.getFileId()), fileService);
        return histories.stream()
                .map(v -> FacilityHistoryResponse.from(v, fileMap.get(v.getFileId())))
                .toList();
    }
}
