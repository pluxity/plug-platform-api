package com.pluxity.domains.device.service;

import com.pluxity.domains.device.dto.Label3DCreateRequest;
import com.pluxity.domains.device.dto.Label3DResponse;
import com.pluxity.domains.device.dto.Label3DUpdateRequest;
import com.pluxity.domains.device.entity.Label3D;
import com.pluxity.domains.device.repository.Label3DRepository;
import com.pluxity.feature.service.FeatureService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Label3DService {

    private final Label3DRepository label3DRepository;
    private final FeatureService featureService;

    @Transactional
    public Label3DResponse createLabel3D(Label3DCreateRequest request) {
        Label3D label3D = Label3D.create(request.displayText());

        if (request.featureId() != null) {
            label3D.changeFeature(featureService.findFeatureById(request.featureId()));
        }

        Label3D savedLabel3D = label3DRepository.save(label3D);
        return Label3DResponse.from(savedLabel3D);
    }

    @Transactional(readOnly = true)
    public Label3DResponse getLabel3DById(Long id) {
        Label3D label3D = findLabel3DById(id);
        return Label3DResponse.from(label3D);
    }

    @Transactional(readOnly = true)
    public List<Label3DResponse> getAllLabel3Ds() {
        return label3DRepository.findAll().stream().map(Label3DResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<Label3DResponse> getLabel3DsByFacilityId(String facilityId) {
        return label3DRepository.findAllByFacilityId(facilityId).stream()
                .map(Label3DResponse::from)
                .toList();
    }

    @Transactional
    public Label3DResponse updateLabel3D(Long id, Label3DUpdateRequest request) {
        Label3D label3D = findLabel3DById(id);

        label3D.update(request.displayText());

        return Label3DResponse.from(label3D);
    }

    @Transactional
    public void deleteLabel3D(Long id) {
        Label3D label3D = findLabel3DById(id);
        label3D.clearAllRelations();
        label3DRepository.delete(label3D);
    }

    @Transactional(readOnly = true)
    public Label3D findLabel3DById(Long id) {
        return label3DRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Label3D not found with id: " + id));
    }
}
