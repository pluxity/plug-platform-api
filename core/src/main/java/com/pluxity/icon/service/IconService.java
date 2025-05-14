package com.pluxity.icon.service;

import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.icon.dto.IconCreateRequest;
import com.pluxity.icon.dto.IconResponse;
import com.pluxity.icon.dto.IconUpdateRequest;
import com.pluxity.icon.entity.Icon;
import com.pluxity.icon.repository.IconRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IconService {

    private final IconRepository repository;
    private final FileService fileService;
    private final IconRepository iconRepository;

    @Transactional(readOnly = true)
    public IconResponse getIcon(Long id) {
        Icon icon = findIconById(id);
        FileResponse fileResponse = getFileResponse(icon);
        return IconResponse.from(icon, fileResponse);
    }

    @Transactional(readOnly = true)
    public List<IconResponse> getIcons() {
        return repository.findAll().stream()
                .map(icon -> IconResponse.from(icon, fileService.getFileResponse(icon.getFileId())))
                .toList();
    }

    @Transactional
    public Long createIcon(IconCreateRequest request) {
        Icon icon = Icon.create(request);
        Icon savedIcon = iconRepository.save(icon);

        if (request.fileId() != null) {
            String filePath = savedIcon.getIconFilePath();
            FileEntity fileEntity = fileService.finalizeUpload(request.fileId(), filePath);
            savedIcon.updateFileEntity(fileEntity);
        }

        return savedIcon.getId();
    }

    @Transactional
    public void update(Long id, IconUpdateRequest request) {
        Icon icon = findIconById(id);
        icon.update(request);

        if (request.fileId() != null) {
            FileEntity fileEntity = fileService.finalizeUpload(request.fileId(), icon.getIconFilePath());
            icon.updateFileEntity(fileEntity);
        }
    }

    @Transactional
    public void delete(Long id) {
        Icon icon = findIconById(id);
        repository.delete(icon);
    }

    private FileResponse getFileResponse(Icon icon) {
        if (!icon.hasFile()) {
            return null;
        }
        return fileService.getFileResponse(icon.getFileId());
    }

    private Icon findIconById(Long id) {
        return repository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Icon not found", HttpStatus.NOT_FOUND, "해당 아이콘을 찾을 수 없습니다: " + id));
    }
}
