package com.pluxity.user.controller;

import com.pluxity.user.dto.TemplateCreateRequest;
import com.pluxity.user.dto.TemplateResponse;
import com.pluxity.user.dto.TemplateUpdateRequest;
import com.pluxity.user.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplate(@PathVariable Long id) {
        TemplateResponse response = templateService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        List<TemplateResponse> responses = templateService.findAll();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Long> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request) {
        return ResponseEntity.ok(templateService.save(request).id());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateUpdateRequest request) {
        TemplateResponse response = templateService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 