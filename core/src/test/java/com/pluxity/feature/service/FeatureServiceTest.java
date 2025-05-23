package com.pluxity.feature.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FeatureServiceTest {

    @Autowired
    private FeatureRepository featureRepository;
    
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private FeatureService featureService;

    @Test
    @DisplayName("유효한 요청으로 피처 생성 시 피처가 저장된다")
    void createFeature_WithValidRequest_SavesFeature() {
        // given
        Spatial position = Spatial.builder().x(1.0).y(2.0).z(3.0).build();
        Spatial rotation = Spatial.builder().x(0.0).y(90.0).z(0.0).build();
        Spatial scale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        FeatureCreateRequest request = FeatureCreateRequest.builder()
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .build();
        
        // when
        FeatureResponse response = featureService.createFeature(request);
        
        // then
        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(position.getX(), response.position().getX());
        assertEquals(position.getY(), response.position().getY());
        assertEquals(position.getZ(), response.position().getZ());
        assertEquals(rotation.getX(), response.rotation().getX());
        assertEquals(rotation.getY(), response.rotation().getY());
        assertEquals(rotation.getZ(), response.rotation().getZ());
        assertEquals(scale.getX(), response.scale().getX());
        assertEquals(scale.getY(), response.scale().getY());
        assertEquals(scale.getZ(), response.scale().getZ());
        
        // 저장된 피처 확인
        List<Feature> features = featureRepository.findAll();
        assertFalse(features.isEmpty());
    }
    
    @Test
    @DisplayName("기본값으로 피처 생성 시 기본 Spatial 값으로 피처가 생성된다")
    void createFeature_WithDefaultValues_CreatesFeatureWithDefaultSpatials() {
        // given
        Spatial defaultPosition = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial defaultRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial defaultScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        FeatureCreateRequest request = FeatureCreateRequest.builder()
                .position(defaultPosition)
                .rotation(defaultRotation)
                .scale(defaultScale)
                .build();
        
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
        Spatial position = Spatial.builder().x(1.0).y(2.0).z(3.0).build();
        Spatial rotation = Spatial.builder().x(0.0).y(90.0).z(0.0).build();
        Spatial scale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature feature = Feature.builder()
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .build();
        
        Feature savedFeature = featureRepository.save(feature);
        
        // when
        FeatureResponse response = featureService.getFeature(savedFeature.getId());
        
        // then
        assertNotNull(response);
        assertEquals(savedFeature.getId(), response.id());
        assertEquals(position.getX(), response.position().getX());
        assertEquals(position.getY(), response.position().getY());
        assertEquals(position.getZ(), response.position().getZ());
        assertEquals(rotation.getX(), response.rotation().getX());
        assertEquals(rotation.getY(), response.rotation().getY());
        assertEquals(rotation.getZ(), response.rotation().getZ());
        assertEquals(scale.getX(), response.scale().getX());
        assertEquals(scale.getY(), response.scale().getY());
        assertEquals(scale.getZ(), response.scale().getZ());
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 피처 조회 시 예외가 발생한다")
    void getFeature_WithNonExistingId_ThrowsException() {
        // given
        Long nonExistingId = 999L;
        
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
        Feature feature1 = Feature.builder()
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .build();
        
        Feature feature2 = Feature.builder()
                .position(Spatial.builder().x(2.0).y(2.0).z(2.0).build())
                .rotation(Spatial.builder().x(90.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(2.0).y(2.0).z(2.0).build())
                .build();
        
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
        Spatial originalPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        Spatial originalRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial originalScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature feature = Feature.builder()
                .position(originalPosition)
                .rotation(originalRotation)
                .scale(originalScale)
                .build();
        
        Feature savedFeature = featureRepository.save(feature);
        
        Spatial newPosition = Spatial.builder().x(2.0).y(2.0).z(2.0).build();
        Spatial newRotation = Spatial.builder().x(90.0).y(0.0).z(0.0).build();
        
        FeatureUpdateRequest request = FeatureUpdateRequest.builder()
                .position(newPosition)
                .rotation(newRotation)
                .build();
        
        // when
        FeatureResponse response = featureService.updateFeature(savedFeature.getId(), request);
        
        // then
        assertNotNull(response);
        assertEquals(savedFeature.getId(), response.id());
        assertEquals(newPosition.getX(), response.position().getX());
        assertEquals(newPosition.getY(), response.position().getY());
        assertEquals(newPosition.getZ(), response.position().getZ());
        assertEquals(newRotation.getX(), response.rotation().getX());
        assertEquals(newRotation.getY(), response.rotation().getY());
        assertEquals(newRotation.getZ(), response.rotation().getZ());
        assertEquals(originalScale.getX(), response.scale().getX());
        assertEquals(originalScale.getY(), response.scale().getY());
        assertEquals(originalScale.getZ(), response.scale().getZ());
        
        // DB에서 업데이트된 값 확인
        Feature updatedFeature = featureRepository.findById(savedFeature.getId()).orElse(null);
        assertNotNull(updatedFeature);
        assertEquals(newPosition.getX(), updatedFeature.getPosition().getX());
        assertEquals(newPosition.getY(), updatedFeature.getPosition().getY());
        assertEquals(newPosition.getZ(), updatedFeature.getPosition().getZ());
    }
    
    @Test
    @DisplayName("부분 업데이트 요청 시 해당 필드만 업데이트된다")
    void updateFeature_WithPartialRequest_UpdatesOnlySpecifiedFields() {
        // given
        Spatial originalPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        Spatial originalRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial originalScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature feature = Feature.builder()
                .position(originalPosition)
                .rotation(originalRotation)
                .scale(originalScale)
                .build();
        
        Feature savedFeature = featureRepository.save(feature);
        
        Spatial newScale = Spatial.builder().x(2.0).y(2.0).z(2.0).build();
        
        FeatureUpdateRequest request = FeatureUpdateRequest.builder()
                .scale(newScale)
                .build();
        
        // when
        FeatureResponse response = featureService.updateFeature(savedFeature.getId(), request);
        
        // then
        assertNotNull(response);
        assertEquals(savedFeature.getId(), response.id());
        
        // 원래 값과 동일한지 확인 (변경되지 않은 필드)
        assertEquals(originalPosition.getX(), response.position().getX());
        assertEquals(originalPosition.getY(), response.position().getY());
        assertEquals(originalPosition.getZ(), response.position().getZ());
        assertEquals(originalRotation.getX(), response.rotation().getX());
        assertEquals(originalRotation.getY(), response.rotation().getY());
        assertEquals(originalRotation.getZ(), response.rotation().getZ());
        
        // 변경된 값 확인
        assertEquals(newScale.getX(), response.scale().getX());
        assertEquals(newScale.getY(), response.scale().getY());
        assertEquals(newScale.getZ(), response.scale().getZ());
        
        // DB에서 업데이트된 값 확인
        Feature updatedFeature = featureRepository.findById(savedFeature.getId()).orElse(null);
        assertNotNull(updatedFeature);
        assertEquals(newScale.getX(), updatedFeature.getScale().getX());
        assertEquals(newScale.getY(), updatedFeature.getScale().getY());
        assertEquals(newScale.getZ(), updatedFeature.getScale().getZ());
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 피처 업데이트 시 예외가 발생한다")
    void updateFeature_WithNonExistingId_ThrowsException() {
        // given
        Long nonExistingId = 999L;
        FeatureUpdateRequest request = FeatureUpdateRequest.builder()
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .build();
        
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
        Feature feature = Feature.builder()
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .build();
        
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
        Long nonExistingId = 999L;
        
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
        Spatial origin = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial position = Spatial.builder().x(1.0).y(2.0).z(3.0).build();
        Spatial defaultScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        // when & then
        assertEquals(0.0, origin.getX());
        assertEquals(0.0, origin.getY());
        assertEquals(0.0, origin.getZ());
        
        assertEquals(1.0, position.getX());
        assertEquals(2.0, position.getY());
        assertEquals(3.0, position.getZ());
        
        assertEquals(1.0, defaultScale.getX());
        assertEquals(1.0, defaultScale.getY());
        assertEquals(1.0, defaultScale.getZ());
    }
    
    @Test
    @DisplayName("Feature와 Asset의 양방향 연관관계 설정 테스트")
    void testFeatureAssetBidirectionalRelationship() {
        // given
        // Asset 생성
        Asset asset = Asset.builder()
                .name("테스트 에셋")
                .build();
        Asset savedAsset = assetRepository.save(asset);
        
        // Feature 생성
        Spatial position = Spatial.builder().x(1.0).y(2.0).z(3.0).build();
        Spatial rotation = Spatial.builder().x(0.0).y(90.0).z(0.0).build();
        Spatial scale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature feature = Feature.builder()
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .build();
        
        Feature savedFeature = featureRepository.save(feature);
        
        // when
        // Feature에 Asset 설정 (양방향 연관관계 설정)
        savedFeature.changeAsset(savedAsset);
        
        // then
        // Feature -> Asset 접근 확인
        assertNotNull(savedFeature.getAsset());
        assertEquals(savedAsset.getId(), savedFeature.getAsset().getId());
        assertEquals("테스트 에셋", savedFeature.getAsset().getName());
        
        // Asset -> Feature 접근 확인 (양방향 연관관계)
        assertNotNull(savedAsset.getFeature());
        assertEquals(savedFeature.getId(), savedAsset.getFeature().getId());
        
        // 관계 해제 테스트
        savedFeature.changeAsset(null);
        
        assertNull(savedFeature.getAsset());
        assertNull(savedAsset.getFeature());
    }
    
    @Test
    @DisplayName("Asset을 가진 Feature 생성 테스트")
    void createFeature_WithAsset_CreatesFeatureWithAsset() {
        // given
        // Asset 생성
        Asset asset = Asset.builder()
                .name("테스트 에셋")
                .build();
        Asset savedAsset = assetRepository.save(asset);
        
        // Feature 생성
        Spatial position = Spatial.builder().x(1.0).y(2.0).z(3.0).build();
        Spatial rotation = Spatial.builder().x(0.0).y(90.0).z(0.0).build();
        Spatial scale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature feature = Feature.builder()
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .asset(savedAsset)  // 생성자에서 Asset 설정
                .build();
        
        // when
        Feature savedFeature = featureRepository.save(feature);
        
        // then
        assertNotNull(savedFeature.getAsset());
        assertEquals(savedAsset.getId(), savedFeature.getAsset().getId());
        assertEquals("테스트 에셋", savedFeature.getAsset().getName());
        
        // Asset -> Feature 접근 확인 (양방향 연관관계)
        assertNotNull(savedAsset.getFeature());
        assertEquals(savedFeature.getId(), savedAsset.getFeature().getId());
    }
}