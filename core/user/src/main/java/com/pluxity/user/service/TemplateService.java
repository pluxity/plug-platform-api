package com.pluxity.user.service;

import com.pluxity.user.dto.TemplateCreateRequest;
import com.pluxity.user.dto.TemplateResponse;
import com.pluxity.user.dto.TemplateUpdateRequest;
import com.pluxity.user.entity.Template;
import com.pluxity.user.repository.TemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Transactional
    public TemplateResponse save(TemplateCreateRequest request) {
        Template template =
                Template.builder().name(request.name()).thumbnail(request.thumbnail()).build();

        Template savedTemplate = templateRepository.save(template);
        return TemplateResponse.from(savedTemplate);
    }

    @Transactional(readOnly = true)
    public TemplateResponse findById(Long id) {
        return TemplateResponse.from(findTemplateById(id));
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> findAll() {
        return templateRepository.findAll().stream().map(TemplateResponse::from).toList();
    }

    @Transactional
    public TemplateResponse update(Long id, TemplateUpdateRequest request) {
        Template template = findTemplateById(id);

        if (request.name() != null && !request.name().isBlank()) {
            template.changeName(request.name());
        }
        if (request.url() != null && !request.url().isBlank()) {
            template.changeUrl(request.url());
        }

        return TemplateResponse.from(template);
    }

    @Transactional
    public void delete(Long id) {
        Template template = findTemplateById(id);
        templateRepository.delete(template);
    }

    Template findTemplateById(Long id) {
        return templateRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + id));
    }
}
