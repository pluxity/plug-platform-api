package com.pluxity.domains.device.service;

import com.pluxity.domains.device.dto.SpaceTextCreateRequest;
import com.pluxity.domains.device.dto.SpaceTextResponse;
import com.pluxity.domains.device.dto.SpaceTextUpdateRequest;
import com.pluxity.domains.device.entity.SpaceText;
import com.pluxity.domains.device.repository.SpaceTextRepository;
import com.pluxity.feature.service.FeatureService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpaceTextService {

    private final SpaceTextRepository spaceTextRepository;
    private final FeatureService featureService;

    @Transactional
    public SpaceTextResponse createSpaceText(SpaceTextCreateRequest request) {

        String id = UUID.randomUUID().toString();
        SpaceText spaceText = SpaceText.create(id, request.textContent());

        if (request.featureId() != null) {
            spaceText.changeFeature(featureService.findFeatureById(request.featureId()));
        }

        SpaceText savedSpaceText = spaceTextRepository.save(spaceText);
        return SpaceTextResponse.from(savedSpaceText);
    }

    @Transactional(readOnly = true)
    public SpaceTextResponse getSpaceTextById(String id) {
        SpaceText spaceText = findSpaceTextById(id);
        return SpaceTextResponse.from(spaceText);
    }

    @Transactional(readOnly = true)
    public List<SpaceTextResponse> getAllSpaceTexts() {
        return spaceTextRepository.findAll().stream().map(SpaceTextResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SpaceTextResponse> getSpaceByFacilityId(String facilityId) {
        return spaceTextRepository.findAllByFacilityId(facilityId).stream()
                .map(SpaceTextResponse::from)
                .toList();
    }

    @Transactional
    public SpaceTextResponse updateSpaceText(String id, SpaceTextUpdateRequest request) {
        SpaceText spaceText = findSpaceTextById(id);

        spaceText.update(request.textContent());

        return SpaceTextResponse.from(spaceText);
    }

    @Transactional
    public void deleteSpaceText(String id) {
        SpaceText spaceText = findSpaceTextById(id);
        spaceText.clearAllRelations();
        spaceTextRepository.delete(spaceText);
    }

    @Transactional(readOnly = true)
    public SpaceText findSpaceTextById(String id) {
        return spaceTextRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpaceText not found with id: " + id));
    }
}
