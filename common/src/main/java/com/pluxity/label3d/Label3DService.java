package com.pluxity.label3d;

import com.pluxity.facility.FacilityService;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.service.FeatureService;
import com.pluxity.global.utils.SortUtils;
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
    private final FacilityService facilityService;

    @Transactional
    public Label3DResponse createLabel3D(Label3DCreateRequest request) {
        Feature feature =
                featureService.saveFeature(
                        Feature.builder()
                                .id(request.id())
                                .facility(facilityService.findById(request.facilityId()))
                                .floorId(request.floorId())
                                .position(request.position())
                                .rotation(request.rotation())
                                .scale(request.scale())
                                .build());

        Label3D label3D = Label3D.createWithFeature(feature, request.displayText());

        Label3D savedLabel3D = label3DRepository.save(label3D);
        return Label3DResponse.from(savedLabel3D);
    }

    @Transactional(readOnly = true)
    public Label3DResponse getLabel3DById(String id) {
        Label3D label3D = findLabel3DById(id);
        return Label3DResponse.from(label3D);
    }

    @Transactional(readOnly = true)
    public List<Label3DResponse> getAllLabel3Ds() {
        return label3DRepository.findAll(SortUtils.getOrderByCreatedAtDesc()).stream()
                .map(Label3DResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Label3DResponse> getLabel3DsByFacilityId(String facilityId) {
        return label3DRepository.findAllByFacilityId(facilityId).stream()
                .map(Label3DResponse::from)
                .toList();
    }

    @Transactional
    public Label3DResponse updateLabel3D(String id, Label3DUpdateRequest request) {
        Label3D label3D = findLabel3DById(id);

        FeatureUpdateRequest featureUpdateRequest =
                new FeatureUpdateRequest(request.position(), request.rotation(), request.scale());
        label3D.getFeature().update(featureUpdateRequest);

        return Label3DResponse.from(label3D);
    }

    @Transactional
    public void deleteLabel3D(String id) {
        Label3D label3D = findLabel3DById(id);
        label3D.clearAllRelations();
        label3DRepository.delete(label3D);
    }

    @Transactional(readOnly = true)
    public Label3D findLabel3DById(String id) {
        return label3DRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Label3D not found with id: " + id));
    }
}
