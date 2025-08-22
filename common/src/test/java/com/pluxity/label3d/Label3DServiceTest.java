package com.pluxity.label3d;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.config.MockBeansConfig;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityRepository;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(MockBeansConfig.class)
@Transactional
class Label3DServiceTest {

    @Autowired private Label3DService label3DService;
    @Autowired private Label3DRepository label3DRepository;

    // 실제 서비스와 리포지토리를 주입받아 사용
    @Autowired private FacilityService facilityService;
    @Autowired private FeatureRepository featureRepository;
    @Autowired private FacilityRepository facilityRepository;

    // Facility 추상 클래스를 상속받는 테스트용 구체 클래스
    @Entity
    @DiscriminatorValue("TEST_FACILITY")
    @NoArgsConstructor
    public static class FacilityInstance extends Facility {
        public FacilityInstance(String name, String code) {
            super(name, code);
        }
    }

    private Long createTestFacility() {
        FacilityCreateRequest request =
                new FacilityCreateRequest("테스트 시설", "FAC01", null, null, null, null, null, null);
        Facility facility = new FacilityInstance(request.name(), request.code());
        return facilityService.save(facility, request).getId();
    }

    @Test
    @DisplayName("성공: 실제 시설과 함께 3D 라벨 생성 시 모든 연관관계와 데이터가 정확히 저장된다")
    void createLabel3D_withRealFacility_savesAllEntitiesCorrectly() {
        // GIVEN: 실제 시설을 DB에 생성
        Long facilityId = createTestFacility();
        String featureId = UUID.randomUUID().toString();

        Label3DCreateRequest request =
                new Label3DCreateRequest(
                        featureId,
                        "실제 라벨",
                        facilityId,
                        "GF",
                        new Spatial(1.0, 2.0, 3.0),
                        new Spatial(4.0, 5.0, 6.0),
                        new Spatial(1.0, 1.0, 1.0));

        // WHEN: 서비스 메서드 호출
        Label3DResponse response = label3DService.createLabel3D(request);

        // THEN
        // 1. 응답 DTO 검증
        assertThat(response.id()).isEqualTo(featureId);
        assertThat(response.displayText()).isEqualTo("실제 라벨");
        assertThat(response.floorId()).isEqualTo("GF");
        assertThat(response.position().getY()).isEqualTo(2.0);

        // 2. Label3D가 DB에 저장되었는지 검증
        Label3D savedLabel = label3DRepository.findById(featureId).orElseThrow();
        assertThat(savedLabel.getDisplayText()).isEqualTo("실제 라벨");

        // 3. Feature가 DB에 저장되었고, Facility와 올바르게 연결되었는지 검증
        Feature savedFeature = featureRepository.findById(featureId).orElseThrow();
        assertThat(savedFeature.getFloorId()).isEqualTo("GF");
        assertThat(savedFeature.getFacility().getId()).isEqualTo(facilityId);
    }

    @Test
    @DisplayName("성공: 3D 라벨 정보 수정 시 DB에 저장된 Feature의 위치 정보가 변경된다")
    void updateLabel3D_withValidRequest_updatesFeatureInDatabase() {
        // GIVEN: 테스트용 3D 라벨을 DB에 생성
        Long facilityId = createTestFacility();
        String featureId = UUID.randomUUID().toString();
        Label3DCreateRequest createRequest =
                new Label3DCreateRequest(
                        featureId,
                        "원본 라벨",
                        facilityId,
                        "1F",
                        new Spatial(0.0, 0.0, 0.0),
                        new Spatial(0.0, 0.0, 0.0),
                        new Spatial(1.0, 1.0, 1.0));
        label3DService.createLabel3D(createRequest);

        // WHEN: 위치와 크기를 변경하는 업데이트 요청
        Spatial newPosition = new Spatial(99.0, 99.0, 99.0);
        Spatial newScale = new Spatial(2.0, 2.0, 2.0);
        Label3DUpdateRequest updateRequest = new Label3DUpdateRequest(newPosition, null, newScale);
        label3DService.updateLabel3D(featureId, updateRequest);

        // THEN: DB에서 직접 엔티티를 조회하여 변경사항 검증
        Feature updatedFeature = featureRepository.findById(featureId).orElseThrow();
        assertThat(updatedFeature.getPosition().getX()).isEqualTo(99);
        assertThat(updatedFeature.getScale().getY()).isEqualTo(2);
        // rotation은 요청에 없었으므로 기존 값(0) 유지
        assertThat(updatedFeature.getRotation().getZ()).isEqualTo(0);

        // 라벨의 텍스트는 변경되지 않음
        Label3D label = label3DRepository.findById(featureId).orElseThrow();
        assertThat(label.getDisplayText()).isEqualTo("원본 라벨");
    }

    @Test
    @DisplayName("성공: 3D 라벨 삭제 시, 연관된 Feature와 Label3D가 모두 DB에서 삭제된다")
    void deleteLabel3D_withExistingId_deletesBothLabelAndFeatureFromDatabase() {
        // GIVEN: 삭제할 3D 라벨 생성
        Long facilityId = createTestFacility();
        String featureId = UUID.randomUUID().toString();
        Label3DCreateRequest createRequest =
                new Label3DCreateRequest(featureId, "삭제될 라벨", facilityId, "B1", null, null, null);
        label3DService.createLabel3D(createRequest);

        // GIVEN: 삭제 전 데이터가 DB에 있는지 확인
        assertThat(label3DRepository.existsById(featureId)).isTrue();
        assertThat(featureRepository.existsById(featureId)).isTrue();

        // WHEN
        label3DService.deleteLabel3D(featureId);

        // THEN: 두 엔티티 모두 DB에서 삭제되었는지 확인
        assertThat(label3DRepository.findById(featureId)).isEmpty();
        assertThat(featureRepository.findById(featureId)).isEmpty();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 시설 ID로 라벨 생성 요청 시 예외가 발생한다")
    void createLabel3D_withNonExistingFacilityId_throwsException() {
        // GIVEN
        Label3DCreateRequest request =
                new Label3DCreateRequest(UUID.randomUUID().toString(), "라벨", 9999L, "1F", null, null, null);

        // WHEN & THEN: 실제 FacilityService가 예외를 던짐
        assertThrows(CustomException.class, () -> label3DService.createLabel3D(request));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 라벨 수정 요청 시 예외가 발생한다")
    void updateLabel3D_withNonExistingId_throwsException() {
        // GIVEN
        Label3DUpdateRequest request = new Label3DUpdateRequest(new Spatial(1.0, 1.0, 1.0), null, null);

        // WHEN & THEN
        assertThrows(
                EntityNotFoundException.class,
                () -> label3DService.updateLabel3D("NON_EXISTING_ID", request));
    }

    @Test
    @DisplayName("성공: 전체 3D 라벨 조회 시 라벨이 없으면 빈 리스트를 반환한다")
    void getAllLabel3Ds_whenNoLabelsExist_returnsEmptyList() {
        // GIVEN: 데이터가 없는 상태

        // WHEN
        var responses = label3DService.getAllLabel3Ds();

        // THEN
        assertThat(responses).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("성공: 특정 시설 ID로 조회 시 해당 시설에 속한 라벨만 반환된다")
    void getLabel3DsByFacilityId_returnsOnlyLabelsOfThatFacility() {
        // GIVEN: 두 개의 다른 시설과 각각의 라벨 생성
        Long facilityId1 = createTestFacility();
        FacilityCreateRequest request =
                new FacilityCreateRequest("테스트 시설", "FAC02", null, null, null, null, null, null);
        Facility facility = new FacilityInstance(request.name(), request.code());
        Long facilityId2 = facilityService.save(facility, request).getId();

        String featureId1 = UUID.randomUUID().toString();
        label3DService.createLabel3D(
                new Label3DCreateRequest(featureId1, "시설1의 라벨", facilityId1, "1F", null, null, null));

        String featureId2 = UUID.randomUUID().toString();
        label3DService.createLabel3D(
                new Label3DCreateRequest(featureId2, "시설2의 라벨", facilityId2, "1F", null, null, null));

        // WHEN: 첫 번째 시설 ID로 조회
        List<Label3DResponse> responses = label3DService.getLabel3DsByFacilityId(facilityId1);

        // THEN
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(featureId1);
        assertThat(responses.getFirst().displayText()).isEqualTo("시설1의 라벨");
    }

    @Test
    @DisplayName("성공: 라벨이 없는 시설 ID로 조회 시 빈 리스트를 반환한다")
    void getLabel3DsByFacilityId_forFacilityWithNoLabels_returnsEmptyList() {
        // GIVEN: 라벨이 없는 시설 생성
        Long facilityIdWithNoLabels = createTestFacility();

        // WHEN
        List<Label3DResponse> responses =
                label3DService.getLabel3DsByFacilityId(facilityIdWithNoLabels);

        // THEN
        assertThat(responses).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("성공: 라벨 수정 시 일부 필드만 null이 아닌 값으로 업데이트된다")
    void updateLabel3D_withPartialNonNullValues_updatesOnlySpecificFields() {
        // GIVEN
        Long facilityId = createTestFacility();
        String featureId = UUID.randomUUID().toString();
        Label3DCreateRequest createRequest =
                new Label3DCreateRequest(
                        featureId,
                        "원본 라벨",
                        facilityId,
                        "1F",
                        new Spatial(1.0, 1.0, 1.0),
                        new Spatial(2.0, 2.0, 2.0),
                        new Spatial(3.0, 3.0, 3.0));
        label3DService.createLabel3D(createRequest);

        // WHEN: rotation 정보만 업데이트
        Spatial newRotation = new Spatial(99.0, 99.0, 99.0);
        Label3DUpdateRequest updateRequest = new Label3DUpdateRequest(null, newRotation, null);
        label3DService.updateLabel3D(featureId, updateRequest);

        // THEN
        Feature updatedFeature = featureRepository.findById(featureId).orElseThrow();
        // position과 scale은 기존 값 유지
        assertThat(updatedFeature.getPosition().getX()).isEqualTo(1.0);
        assertThat(updatedFeature.getScale().getX()).isEqualTo(3.0);
        // rotation은 새로운 값으로 변경
        assertThat(updatedFeature.getRotation().getX()).isEqualTo(99.0);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 라벨 삭제 요청 시 예외가 발생한다")
    void deleteLabel3D_withNonExistingId_throwsException() {
        // WHEN & THEN
        assertThrows(
                EntityNotFoundException.class, () -> label3DService.deleteLabel3D("NON_EXISTING_ID"));
    }
}
