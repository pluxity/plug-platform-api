package com.pluxity.feature.controller;

import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.service.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @PostMapping
    public ResponseEntity<FeatureResponse> createFeature(@RequestBody FeatureCreateRequest request) {
        FeatureResponse response = featureService.createFeature(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FeatureResponse>> getFeatures() {
        List<FeatureResponse> responses = featureService.getFeatures();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FeatureResponse> getFeature(@PathVariable Long id) {
        FeatureResponse response = featureService.getFeature(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponse> updateFeature(
            @PathVariable Long id, 
            @RequestBody FeatureUpdateRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/position")
    public ResponseEntity<FeatureResponse> updatePosition(
            @PathVariable Long id, 
            @RequestBody FeatureUpdateRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/rotation")
    public ResponseEntity<FeatureResponse> updateRotation(
            @PathVariable Long id, 
            @RequestBody FeatureUpdateRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/scale")
    public ResponseEntity<FeatureResponse> updateScale(
            @PathVariable Long id, 
            @RequestBody FeatureUpdateRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeature(@PathVariable Long id) {
        featureService.deleteFeature(id);
        return ResponseEntity.noContent().build();
    }
}
