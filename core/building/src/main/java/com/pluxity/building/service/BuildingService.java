package com.pluxity.building.service;

import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.entity.Building;
import com.pluxity.building.repository.BuildingRepository;
import com.pluxity.file.dto.FileUploadResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static com.pluxity.global.constant.ErrorCode.FAILED_TO_UPLOAD_FILE;
import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {

    private final FileService fileService;

    private final BuildingRepository repository;

    @Transactional
    public Long save(BuildingCreateRequest dto) {

        var building = Building.builder()
                                .name(dto.name())
                                .build();

        var savedBuilding = repository.save(building);

        try {
            FileEntity file = fileService.finalizeUpload(dto.fileId(), "drawings/" + savedBuilding.getId() + "/");
            building.updateFile(file);
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", savedBuilding.getId(), e);
            throw new CustomException(FAILED_TO_UPLOAD_FILE, "파일 업로드 실패");
        }

        return savedBuilding.getId();
    }

}
