package com.pluxity.label3d

import com.pluxity.config.MockBeansConfig
import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.feature.entity.Spatial
import com.pluxity.feature.repository.FeatureRepository
import com.pluxity.feature.service.FeatureAssignment
import com.pluxity.global.exception.CustomException
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class Label3DServiceTest
    @Autowired
    constructor(
        private val label3DService: Label3DService,
        private val label3DRepository: Label3DRepository,
        private val facilityService: FacilityService,
        private val featureRepository: FeatureRepository,
    ) {
        @MockitoBean
        lateinit var featureAssignment: FeatureAssignment

        // Facility 추상 클래스를 상속받는 테스트용 구체 클래스
        @Entity
        @DiscriminatorValue("TEST_FACILITY")
        class FacilityInstance(
            name: String,
            code: String? = null,
        ) : Facility(name, code)

        private fun createTestFacility(): Long {
            val request =
                FacilityCreateRequest("테스트 시설", "FAC01", null, null, null, null, null, null)
            val facility = FacilityInstance(name = request.name, code = request.code)
            return facilityService.save(facility, request).requiredId
        }

        @Test
        @DisplayName("성공: 실제 시설과 함께 3D 라벨 생성 시 모든 연관관계와 데이터가 정확히 저장된다")
        fun createLabel3D_withRealFacility_savesAllEntitiesCorrectly() {
            // GIVEN: 실제 시설을 DB에 생성
            val facilityId = createTestFacility()
            val featureId = UUID.randomUUID().toString()

            val request =
                Label3DCreateRequest(
                    featureId,
                    "실제 라벨",
                    facilityId,
                    "GF",
                    Spatial(1.0, 2.0, 3.0),
                    Spatial(4.0, 5.0, 6.0),
                    Spatial(1.0, 1.0, 1.0),
                )

            // WHEN: 서비스 메서드 호출
            val id = label3DService.createLabel3D(request)
            val response = label3DService.getLabel3DById(id)

            // THEN
            // 1. 응답 DTO 검증
            Assertions.assertThat(response.id).isEqualTo(featureId)
            Assertions.assertThat(response.displayText).isEqualTo("실제 라벨")
            Assertions.assertThat(response.floorId).isEqualTo("GF")
            Assertions.assertThat(response.position?.y).isEqualTo(2.0)

            // 2. Label3D가 DB에 저장되었는지 검증
            val savedLabel = label3DRepository.findById(featureId).orElseThrow()
            Assertions.assertThat(savedLabel.displayText).isEqualTo("실제 라벨")

            // 3. Feature가 DB에 저장되었고, Facility와 올바르게 연결되었는지 검증
            val savedFeature = featureRepository.findById(featureId).orElseThrow()
            Assertions.assertThat(savedFeature.floorId).isEqualTo("GF")
            Assertions.assertThat(savedFeature.facility.id).isEqualTo(facilityId)
        }

        @Test
        @DisplayName("성공: 3D 라벨 정보 수정 시 DB에 저장된 Feature의 위치 정보가 변경된다")
        fun updateLabel3D_withValidRequest_updatesFeatureInDatabase() {
            // GIVEN: 테스트용 3D 라벨을 DB에 생성
            val facilityId = createTestFacility()
            val featureId = UUID.randomUUID().toString()
            val createRequest =
                Label3DCreateRequest(
                    featureId,
                    "원본 라벨",
                    facilityId,
                    "1F",
                    Spatial(0.0, 0.0, 0.0),
                    Spatial(0.0, 0.0, 0.0),
                    Spatial(1.0, 1.0, 1.0),
                )
            label3DService.createLabel3D(createRequest)

            // WHEN: 위치와 크기를 변경하는 업데이트 요청
            val newPosition = Spatial(99.0, 99.0, 99.0)
            val newScale = Spatial(2.0, 2.0, 2.0)
            val updateRequest = Label3DUpdateRequest(newPosition, null, newScale)
            label3DService.updateLabel3D(featureId, updateRequest)

            // THEN: DB에서 직접 엔티티를 조회하여 변경사항 검증
            val updatedFeature = featureRepository.findById(featureId).orElseThrow()
            Assertions.assertThat(updatedFeature.position?.x).isEqualTo(99.0)
            Assertions.assertThat(updatedFeature.scale?.y).isEqualTo(2.0)
            // rotation은 요청에 없었으므로 기존 값(0) 유지
            Assertions.assertThat(updatedFeature.rotation?.z).isEqualTo(0.0)

            // 라벨의 텍스트는 변경되지 않음
            val label = label3DRepository.findById(featureId).orElseThrow()
            Assertions.assertThat(label.displayText).isEqualTo("원본 라벨")
        }

        @Test
        @DisplayName("성공: 3D 라벨 삭제 시, 연관된 Feature와 Label3D가 모두 DB에서 삭제된다")
        fun deleteLabel3D_withExistingId_deletesBothLabelAndFeatureFromDatabase() {
            // GIVEN: 삭제할 3D 라벨 생성
            val facilityId = createTestFacility()
            val featureId = UUID.randomUUID().toString()
            val createRequest =
                Label3DCreateRequest(featureId, "삭제될 라벨", facilityId, "B1", null, null, null)
            label3DService.createLabel3D(createRequest)

            // GIVEN: 삭제 전 데이터가 DB에 있는지 확인
            Assertions.assertThat(label3DRepository.existsById(featureId)).isTrue()
            Assertions.assertThat(featureRepository.existsById(featureId)).isTrue()

            // WHEN
            label3DService.deleteLabel3D(featureId)

            // THEN: 두 엔티티 모두 DB에서 삭제되었는지 확인
            Assertions.assertThat(label3DRepository.findById(featureId)).isEmpty()
            Assertions.assertThat(featureRepository.findById(featureId)).isEmpty()
        }

        @Test
        @DisplayName("실패: 존재하지 않는 시설 ID로 라벨 생성 요청 시 예외가 발생한다")
        fun createLabel3D_withNonExistingFacilityId_throwsException() {
            // GIVEN
            val request =
                Label3DCreateRequest(UUID.randomUUID().toString(), "라벨", 9999L, "1F", null, null, null)

            // WHEN & THEN: 실제 FacilityService가 예외를 던짐
            assertThrows<CustomException> {
                label3DService.createLabel3D(request)
            }
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 라벨 수정 요청 시 예외가 발생한다")
        fun updateLabel3D_withNonExistingId_throwsException() {
            // GIVEN
            val request = Label3DUpdateRequest(Spatial(1.0, 1.0, 1.0), null, null)

            // WHEN & THEN
            assertThrows<CustomException> {
                label3DService.updateLabel3D("NON_EXISTING_ID", request)
            }
        }

        @Test
        @DisplayName("성공: 전체 3D 라벨 조회 시 라벨이 없으면 빈 리스트를 반환한다")
        fun getAllLabel3Ds_whenNoLabelsExist_returnsEmptyList() {
            // GIVEN: 데이터가 없는 상태

            // WHEN

            val responses = label3DService.getAllLabel3Ds()

            // THEN
            Assertions.assertThat(responses).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공: 특정 시설 ID로 조회 시 해당 시설에 속한 라벨만 반환된다")
        fun getLabel3DsByFacilityId_returnsOnlyLabelsOfThatFacility() {
            // GIVEN: 두 개의 다른 시설과 각각의 라벨 생성
            val facilityId1 = createTestFacility()
            val request =
                FacilityCreateRequest("테스트 시설", "FAC02", null, null, null, null, null, null)
            val facility = FacilityInstance(request.name, request.code)
            val facilityId2 = facilityService.save(facility, request).requiredId

            val featureId1 = UUID.randomUUID().toString()
            label3DService.createLabel3D(
                Label3DCreateRequest(featureId1, "시설1의 라벨", facilityId1, "1F", null, null, null),
            )

            val featureId2 = UUID.randomUUID().toString()
            label3DService.createLabel3D(
                Label3DCreateRequest(featureId2, "시설2의 라벨", facilityId2, "1F", null, null, null),
            )

            // WHEN: 첫 번째 시설 ID로 조회
            val responses = label3DService.getLabel3DsByFacilityId(facilityId1)

            // THEN
            Assertions.assertThat(responses).hasSize(1)
            Assertions.assertThat(responses.first().id).isEqualTo(featureId1)
            Assertions.assertThat(responses.first().displayText).isEqualTo("시설1의 라벨")
        }

        @Test
        @DisplayName("성공: 라벨이 없는 시설 ID로 조회 시 빈 리스트를 반환한다")
        fun getLabel3DsByFacilityId_forFacilityWithNoLabels_returnsEmptyList() {
            // GIVEN: 라벨이 없는 시설 생성
            val facilityIdWithNoLabels = createTestFacility()

            // WHEN
            val responses = label3DService.getLabel3DsByFacilityId(facilityIdWithNoLabels)

            // THEN
            Assertions.assertThat(responses).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공: 라벨 수정 시 일부 필드만 null이 아닌 값으로 업데이트된다")
        fun updateLabel3D_withPartialNonNullValues_updatesOnlySpecificFields() {
            // GIVEN
            val facilityId = createTestFacility()
            val featureId = UUID.randomUUID().toString()
            val createRequest =
                Label3DCreateRequest(
                    featureId,
                    "원본 라벨",
                    facilityId,
                    "1F",
                    Spatial(1.0, 1.0, 1.0),
                    Spatial(2.0, 2.0, 2.0),
                    Spatial(3.0, 3.0, 3.0),
                )
            label3DService.createLabel3D(createRequest)

            // WHEN: rotation 정보만 업데이트
            val newRotation = Spatial(99.0, 99.0, 99.0)
            val updateRequest = Label3DUpdateRequest(null, newRotation, null)
            label3DService.updateLabel3D(featureId, updateRequest)

            // THEN
            val updatedFeature = featureRepository.findById(featureId).orElseThrow()
            // position과 scale은 기존 값 유지
            Assertions.assertThat(updatedFeature.position?.x).isEqualTo(1.0)
            Assertions.assertThat(updatedFeature.scale?.x).isEqualTo(3.0)
            // rotation은 새로운 값으로 변경
            Assertions.assertThat(updatedFeature.rotation?.x).isEqualTo(99.0)
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 라벨 삭제 요청 시 예외가 발생한다")
        fun deleteLabel3D_withNonExistingId_throwsException() {
            // WHEN & THEN
            assertThrows<CustomException> {
                label3DService.deleteLabel3D("NON_EXISTING_ID")
            }
        }
    }
