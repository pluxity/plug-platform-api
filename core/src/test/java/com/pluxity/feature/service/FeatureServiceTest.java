package com.pluxity.feature.service;

import com.pluxity.CoreApplicationTest;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeatureServiceTest extends CoreApplicationTest {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private FeatureService featureService;

    @Test
    @DisplayName("유효한 요청으로 피처 생성 시 피처가 저장된다")
    void createFeature_WithValidRequest_SavesFeature() {
        // given
        FeatureCreateRequest request = createFeatureRequest();
        
        // when
        FeatureResponse response = featureService.createFeature(request);
        
        // then
        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(1.0, response.position().getX());
        assertEquals(2.0, response.position().getY());
        assertEquals(3.0, response.position().getZ());
        assertEquals(0.0, response.rotation().getX());
        assertEquals(90.0, response.rotation().getY());
        assertEquals(0.0, response.rotation().getZ());
        assertEquals(1.0, response.scale().getX());
        assertEquals(1.0, response.scale().getY());
        assertEquals(1.0, response.scale().getZ());
        
        // 저장된 피처 확인
        List<Feature> features = featureRepository.findAll();
        assertFalse(features.isEmpty());
    }
    
    @Test
    @DisplayName("기본값으로 피처 생성 시 기본 Spatial 값으로 피처가 생성된다")
    void createFeature_WithDefaultValues_CreatesFeatureWithDefaultSpatials() {
        // given
        Spatial defaultPosition = createDefaultPosition();
        Spatial defaultRotation = createDefaultRotation();
        Spatial defaultScale = createDefaultScale();
        
        FeatureCreateRequest request = createFeatureRequest(defaultPosition, defaultRotation, defaultScale);
        
        // when
        FeatureResponse response = featureService.createFeature(request);
        
        // then
        assertNotNull(response);
        assertEquals(0.0, response.position().getX());
        assertEquals(0.0, response.position().getY());
        assertEquals(0.0, response.position().getZ());
        
        assertEquals(0.0, response.rotation().getX());
        assertEquals(0.0, response.rotation().getY());
        assertEquals(0.0, response.rotation().getZ());
        
        assertEquals(1.0, response.scale().getX());
        assertEquals(1.0, response.scale().getY());
        assertEquals(1.0, response.scale().getZ());
    }
    
    @Test
    @DisplayName("존재하는 ID로 피처 조회 시 피처 정보가 반환된다")
    void getFeature_WithExistingId_ReturnsFeature() {
        // given
        Feature feature = createFeature();
        Feature savedFeature = featureRepository.save(feature);
        
        // when
        FeatureResponse response = featureService.getFeature(savedFeature.getId());
        
        // then
        assertNotNull(response);
        assertEquals(savedFeature.getId(), response.id());
        assertEquals(1.0, response.position().getX());
        assertEquals(2.0, response.position().getY());
        assertEquals(3.0, response.position().getZ());
        assertEquals(0.0, response.rotation().getX());
        assertEquals(90.0, response.rotation().getY());
        assertEquals(0.0, response.rotation().getZ());
        assertEquals(1.0, response.scale().getX());
        assertEquals(1.0, response.scale().getY());
        assertEquals(1.0, response.scale().getZ());
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 피처 조회 시 예외가 발생한다")
    void getFeature_WithNonExistingId_ThrowsException() {
        // given
        Long nonExistingId = DEFAULT_NON_EXISTING_ID;
        
        // when & then
        CustomException exception = assertThrows(CustomException.class, 
                () -> featureService.getFeature(nonExistingId));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Feature not found", exception.getCodeName());
    }
    
    @Test
    @DisplayName("모든 피처 조회 시 피처 목록이 반환된다")
    void getFeatures_ReturnsListOfFeatures() {
        // given
        Feature feature1 = createFeature();
        
        Spatial position2 = createSpatial(2.0, 2.0, 2.0);
        Spatial rotation2 = createSpatial(90.0, 0.0, 0.0);
        Spatial scale2 = createSpatial(2.0, 2.0, 2.0);
        Feature feature2 = createFeature(position2, rotation2, scale2);
        
        featureRepository.save(feature1);
        featureRepository.save(feature2);
        
        // when
        List<FeatureResponse> responses = featureService.getFeatures();
        
        // then
        assertEquals(2, responses.size());
        
        FeatureResponse firstResponse = responses.stream()
                .filter(r -> r.position().getX() == 1.0)
                .findFirst()
                .orElse(null);
        
        FeatureResponse secondResponse = responses.stream()
                .filter(r -> r.position().getX() == 2.0)
                .findFirst()
                .orElse(null);
        
        assertNotNull(firstResponse);
        assertNotNull(secondResponse);
        
        assertEquals(1.0, firstResponse.scale().getX());
        assertEquals(2.0, secondResponse.scale().getX());
    }
    
    @Test
    @DisplayName("피처가 없는 경우 빈 목록이 반환된다")
    void getFeatures_WithNoFeatures_ReturnsEmptyList() {
        // given
        featureRepository.deleteAll();
        
        // when
        List<FeatureResponse> responses = featureService.getFeatures();
        
        // then
        assertTrue(responses.isEmpty());
    }
    
    @Test
    @DisplayName("유효한 요청으로 피처 업데이트 시 피처 정보가 업데이트된다")
    void updateFeature_WithValidRequest_UpdatesFeature() {
        // given
        Feature feature = createFeature();
        Feature savedFeature = featureRepository.save(feature);
        
        FeatureUpdateRequest request = createFeatureUpdateRequest();
        
        // when
        FeatureResponse response = featureService.updateFeature(savedFeature.getId(), request);
        
        // then
        assertNotNull(response);
        assertEquals(savedFeature.getId(), response.id());
        assertEquals(2.0, response.position().getX());
        assertEquals(2.0, response.position().getY());
        assertEquals(2.0, response.position().getZ());
        assertEquals(90.0, response.rotation().getX());
        assertEquals(0.0, response.rotation().getY());
        assertEquals(0.0, response.rotation().getZ());
        assertEquals(1.0, response.scale().getX());
        assertEquals(1.0, response.scale().getY());
        assertEquals(1.0, response.scale().getZ());
        
        // DB에서 업데이트된 값 확인
        Feature updatedFeature = featureRepository.findById(savedFeature.getId()).orElse(null);
        assertNotNull(updatedFeature);
        assertEquals(2.0, updatedFeature.getPosition().getX());
        assertEquals(2.0, updatedFeature.getPosition().getY());
        assertEquals(2.0, updatedFeature.getPosition().getZ());
    }
    
    @Test
    @DisplayName("부분 업데이트 요청 시 해당 필드만 업데이트된다")
    void updateFeature_WithPartialRequest_UpdatesOnlySpecifiedFields() {
        // given
        Feature feature = createFeature();
        Feature savedFeature = featureRepository.save(feature);
        
        FeatureUpdateRequest request = createScaleOnlyUpdateRequest();
        
        // when
        FeatureResponse response = featureService.updateFeature(savedFeature.getId(), request);
        
        // then
        assertNotNull(response);
        assertEquals(savedFeature.getId(), response.id());
        
        // 원래 값과 동일한지 확인 (변경되지 않은 필드)
        assertEquals(1.0, response.position().getX());
        assertEquals(2.0, response.position().getY());
        assertEquals(3.0, response.position().getZ());
        assertEquals(0.0, response.rotation().getX());
        assertEquals(90.0, response.rotation().getY());
        assertEquals(0.0, response.rotation().getZ());
        
        // 변경된 값 확인
        assertEquals(2.0, response.scale().getX());
        assertEquals(2.0, response.scale().getY());
        assertEquals(2.0, response.scale().getZ());
        
        // DB에서 업데이트된 값 확인
        Feature updatedFeature = featureRepository.findById(savedFeature.getId()).orElse(null);
        assertNotNull(updatedFeature);
        assertEquals(2.0, updatedFeature.getScale().getX());
        assertEquals(2.0, updatedFeature.getScale().getY());
        assertEquals(2.0, updatedFeature.getScale().getZ());
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 피처 업데이트 시 예외가 발생한다")
    void updateFeature_WithNonExistingId_ThrowsException() {
        // given
        Long nonExistingId = DEFAULT_NON_EXISTING_ID;
        FeatureUpdateRequest request = createFeatureUpdateRequest();
        
        // when & then
        CustomException exception = assertThrows(CustomException.class, 
                () -> featureService.updateFeature(nonExistingId, request));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Feature not found", exception.getCodeName());
    }
    
    @Test
    @DisplayName("존재하는 ID로 피처 삭제 시 피처가 삭제된다")
    void deleteFeature_WithExistingId_DeletesFeature() {
        // given
        Feature feature = createFeature();
        Feature savedFeature = featureRepository.save(feature);
        Long featureId = savedFeature.getId();
        
        // when
        featureService.deleteFeature(featureId);
        
        // then
        assertTrue(featureRepository.findById(featureId).isEmpty());
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 피처 삭제 시 예외가 발생한다")
    void deleteFeature_WithNonExistingId_ThrowsException() {
        // given
        Long nonExistingId = DEFAULT_NON_EXISTING_ID;
        
        // when & then
        CustomException exception = assertThrows(CustomException.class, 
                () -> featureService.deleteFeature(nonExistingId));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Feature not found", exception.getCodeName());
    }
    
    @Test
    @DisplayName("3D 공간 속성 테스트")
    void testSpatialProperties() {
        // given
        Spatial origin = createDefaultPosition();
        Spatial position = createTestPosition();
        Spatial scale = createDefaultScale();
        
        // when & then
        assertEquals(0.0, origin.getX());
        assertEquals(0.0, origin.getY());
        assertEquals(0.0, origin.getZ());
        
        assertEquals(1.0, position.getX());
        assertEquals(2.0, position.getY());
        assertEquals(3.0, position.getZ());
        
        assertEquals(1.0, scale.getX());
        assertEquals(1.0, scale.getY());
        assertEquals(1.0, scale.getZ());
    }
}