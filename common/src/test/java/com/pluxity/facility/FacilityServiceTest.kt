package com.pluxity.facility

import com.pluxity.config.MockBeansConfig
import com.pluxity.facility.dto.*
import com.pluxity.facility.floor.dto.FloorRequest
import com.pluxity.facility.history.FacilityHistoryService
import com.pluxity.facility.path.FacilityPathService
import com.pluxity.facility.strategy.FloorService
import com.pluxity.global.exception.CustomException
import com.pluxity.util.TestFileUploader
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import lombok.NoArgsConstructor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class FacilityServiceTest {
    @Autowired
    private val facilityService: FacilityService? = null

    @Autowired
    private val facilityRepository: FacilityRepository? = null

    @Autowired
    private val testFileUploader: TestFileUploader? = null

    @MockitoBean
    private val facilityHistoryService: FacilityHistoryService? = null

    @MockitoBean
    private val facilityPathService: FacilityPathService? = null

    @MockitoBean
    private val floorService: FloorService? = null

    // Facility 추상 클래스를 상속받는 테스트용 구체 클래스
    @Entity
    @DiscriminatorValue("TEST")
    @NoArgsConstructor
    class FacilityInstance(name: String?, code: String?, description: String?, drawingFileId: Long?, thumbnailFileId: Long?) :
        Facility(name, code, description, drawingFileId, thumbnailFileId)

    // --- 1. save (생성) 테스트 ---
    @Test
    @DisplayName("성공: 유효한 요청으로 시설 생성 시 모든 필드가 정상적으로 저장된다")
    fun save_withValidRequest_savesFacility() {
        // GIVEN
        val drawingFileId = testFileUploader!!.initiateTestFileUpload("drawing.dwg")
        val thumbnailFileId = testFileUploader.initiateTestFileUpload("thumb.png")
        val request =
            FacilityCreateRequest(
                "서울역",
                "SEOUL_ST",
                "대한민국 수도의 관문",
                drawingFileId,
                thumbnailFileId,
                126.97,
                37.55,
                "{'floor': 5}"
            )
        val facility =
            FacilityInstance(
                request.name,
                request.code,
                request.description,
                request.drawingFileId,
                request.thumbnailFileId
            )

        // WHEN
        val savedFacility = facilityService!!.save(facility, request)

        // THEN
        assertThat(savedFacility.getId()).isNotNull()
        assertThat(savedFacility.getName()).isEqualTo("서울역")
        assertThat(savedFacility.getCode()).isEqualTo("SEOUL_ST")
        assertThat(savedFacility.getDescription()).isEqualTo("대한민국 수도의 관문")
        assertThat(savedFacility.getDrawingFileId()).isEqualTo(drawingFileId)
        assertThat(savedFacility.getThumbnailFileId()).isEqualTo(thumbnailFileId)
        assertThat(savedFacility.getPosition().getLon()).isEqualTo(126.97)
        assertThat(savedFacility.getPosition().getLat()).isEqualTo(37.55)
        assertThat(savedFacility.getPosition().getLocationMeta()).isEqualTo("{'floor': 5}")

        // Mock 객체 호출 검증
        Mockito.verify<FacilityHistoryService?>(facilityHistoryService, Mockito.times(1)).save(drawingFileId, savedFacility.getId(), "최초등록")
    }

    @Test
    @DisplayName("실패: 중복된 코드로 시설 생성 시 예외가 발생한다")
    fun save_withDuplicateCode_throwsCustomException() {
        // GIVEN
        facilityService!!.save(
            FacilityInstance("시설1", "DUP_CODE", null, null, null),
            FacilityCreateRequest("시설1", "DUP_CODE", null, null, null, null, null, null)
        )

        val duplicateRequest =
            FacilityCreateRequest("시설2", "DUP_CODE", null, null, null, null, null, null)
        val facility2 = FacilityInstance("시설2", "DUP_CODE", null, null, null)

        // WHEN & THEN
        Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { facilityService.save(facility2, duplicateRequest) })
    }

    // --- 2. find (조회) 테스트 ---
    @Test
    @DisplayName("실패: 존재하지 않는 ID로 조회 시 예외가 발생한다")
    fun findById_withNonExistingId_throwsCustomException() {
        Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { facilityService!!.findById(9999L) })
    }

    // --- 3. update (PATCH 스타일 수정) 테스트 ---
    @Test
    @DisplayName("성공: update 요청 시 일부 필드만 정상적으로 수정된다")
    fun update_withPartialRequest_updatesOnlyProvidedFields() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("원본 이름", "ORI_CODE", "원본 설명", null, null),
                FacilityCreateRequest("원본 이름", "ORI_CODE", "원본 설명", null, null, 1.0, 1.0, null)
            )

        val request =
            FacilityUpdateRequest("수정된 이름", null, null, null, 2.0, null, null)

        // WHEN
        facilityService.update(saved.getId(), request)

        // THEN
        val updated = facilityService.findById(saved.getId())
        assertThat(updated.getName()).isEqualTo("수정된 이름") // 변경된 필드
        assertThat(updated.getPosition().getLon()).isEqualTo(2.0) // 변경된 필드
        assertThat(updated.getCode()).isEqualTo("ORI_CODE") // 유지된 필드
        assertThat(updated.getDescription()).isEqualTo("원본 설명") // 유지된 필드
    }

    // --- 4. putUpdate (PUT 스타일 수정) 테스트 ---
    @Test
    @DisplayName("성공: putUpdate 요청 시 모든 필드가 요청대로 덮어쓰기된다 (null 포함)")
    fun putUpdate_withFullRequest_overwritesAllFields() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("원본 이름", "ORI_CODE", "원본 설명", null, null),
                FacilityCreateRequest("원본 이름", "ORI_CODE", "원본 설명", null, null, 1.0, 1.0, null)
            )

        // description을 null로 하여 덮어쓰기 테스트
        val request =
            FacilityUpdateRequest("수정된 이름", "UPD_CODE", null, null, 2.0, 2.0, "{}")

        // WHEN
        facilityService.putUpdate(saved.getId(), request)

        // THEN
        val updated = facilityService.findById(saved.getId())
        assertThat(updated.getName()).isEqualTo("수정된 이름")
        assertThat(updated.getCode()).isEqualTo("UPD_CODE")
        assertThat(updated.getDescription()).isNull() // null로 덮어쓰기 되었는지 확인
        assertThat(updated.getPosition().getLat()).isEqualTo(2.0)
    }

    // --- 5. delete (삭제) 테스트 ---
    @Test
    @DisplayName("성공: 시설 삭제 시 DB에서 소프트 삭제된다")
    fun deleteFacility_withExistingId_softDeletesFacility() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("삭제될 시설", "DEL_CODE", null, null, null),
                FacilityCreateRequest("삭제될 시설", "DEL_CODE", null, null, null, null, null, null)
            )

        // WHEN
        facilityService.deleteFacility(saved.getId())

        // THEN
        // SoftDelete 이므로 findById는 예외를 던져야 함
        Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { facilityService.findById(saved.getId()) })
        // Repository 레벨에서는 여전히 존재해야 함 (필요 시 네이티브 쿼리 등으로 확인 가능)
    }

    // --- 6. 도면/경로/위치/층 등 서브 도메인 관련 메서드 테스트 ---
    @Test
    @DisplayName("성공: 도면 파일 업데이트 시 히스토리가 기록된다")
    fun updateDrawingFile_updatesFileAndSavesHistory() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("시설", "CODE", null, null, null),
                FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null)
            )
        val newDrawingFileId = testFileUploader!!.initiateTestFileUpload("new_drawing.dwg")
        val request =
            FacilityDrawingUpdateRequest(newDrawingFileId, "도면 교체")

        // WHEN
        facilityService.updateDrawingFile(saved.getId(), request)

        // THEN
        val updated = facilityService.findById(saved.getId())
        assertThat(updated.getDrawingFileId()).isEqualTo(newDrawingFileId)

        Mockito.verify<FacilityHistoryService?>(facilityHistoryService, Mockito.times(1)).save(newDrawingFileId, saved.getId(), "도면 교체")
    }

    @Test
    @DisplayName("성공: 경로 저장 시 FacilityPathService가 호출된다")
    fun savePath_delegatesToPathService() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("시설", "CODE", null, null, null),
                FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null)
            )
        val request = FacilityPathSaveRequest("주 경로", "MAIN", "{}")

        // WHEN
        facilityService.savePath(saved.getId(), request)

        // THEN
        Mockito.verify<FacilityPathService?>(facilityPathService, Mockito.times(1))
            .save(
                ArgumentMatchers.any<Facility?>(Facility::class.java),
                ArgumentMatchers.eq<String?>("주 경로"),
                ArgumentMatchers.eq<String?>("MAIN"),
                ArgumentMatchers.eq<String?>("{}")
            )
    }

    @Test
    @DisplayName("성공: 층 정보 업데이트 시 FloorService가 호출된다")
    fun updateFloor_delegatesToFloorService() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("시설", "CODE", null, null, null),
                FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null)
            )
        val request = FacilityFloorUpdateRequest(mutableListOf<FloorRequest?>())

        // WHEN
        facilityService.updateFloor(saved.getId(), request)

        // THEN
        Mockito.verify<FloorService?>(floorService, Mockito.times(1))
            .update<Facility?>(ArgumentMatchers.any<Facility?>(Facility::class.java), ArgumentMatchers.anyList<FloorRequest?>())
    }

    @Test
    @DisplayName("실패: 존재하지 않는 시설의 경로 저장 시 예외가 발생한다")
    fun savePath_onNonExistingFacility_throwsCustomException() {
        // GIVEN
        val request = FacilityPathSaveRequest("주 경로", "MAIN", "{}")

        // WHEN & THEN
        Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { facilityService!!.savePath(9999L, request) })
    }

    @Test
    @DisplayName("성공: 일부 선택적 필드가 null일 때도 시설 생성이 성공한다")
    fun save_withNullOptionalFields_succeeds() {
        // GIVEN: code, description, files, location 정보가 모두 null인 요청
        val request =
            FacilityCreateRequest("필수 필드만 있는 시설", "MANDATORY", null, null, null, null, null, null)
        val facility =
            FacilityInstance(request.name, request.code, null, null, null)

        // WHEN
        val savedFacility = facilityService!!.save(facility, request)

        // THEN
        org.assertj.core.api.Assertions.assertThat<Facility?>(savedFacility).isNotNull()
        assertThat(savedFacility.getName()).isEqualTo("필수 필드만 있는 시설")
        assertThat(savedFacility.getCode()).isEqualTo("MANDATORY")
        assertThat(savedFacility.getDescription()).isNull()
        assertThat(savedFacility.getDrawingFileId()).isNull()
        assertThat(savedFacility.getPosition()).isNotNull() // Embedded 객체는 생성됨
    }

    @Test
    @DisplayName("실패: update 시 다른 시설이 사용 중인 코드로 변경하면 예외가 발생한다")
    fun update_withExistingCodeOfAnotherFacility_throwsCustomException() {
        // GIVEN: 두 개의 시설 생성
        facilityService!!.save(
            FacilityInstance("시설1", "CODE1", null, null, null),
            FacilityCreateRequest("시설1", "CODE1", null, null, null, null, null, null)
        )
        val saved2 =
            facilityService.save(
                FacilityInstance("시설2", "CODE2", null, null, null),
                FacilityCreateRequest("시설2", "CODE2", null, null, null, null, null, null)
            )

        // WHEN & THEN: 시설2의 코드를 시설1의 코드로 변경 시도
        val request =
            FacilityUpdateRequest(null, "CODE1", null, null, null, null, null)
        Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { facilityService.update(saved2.getId(), request) })
    }

    @Test
    @DisplayName("성공: findAll 호출 시 모든 시설 목록을 반환한다")
    fun findAll_whenFacilitiesExist_returnsListOfFacilities() {
        // GIVEN
        facilityService!!.save(
            FacilityInstance("시설1", "CODE1", null, null, null),
            FacilityCreateRequest("시설1", "CODE1", null, null, null, null, null, null)
        )
        facilityService.save(
            FacilityInstance("시설2", "CODE2", null, null, null),
            FacilityCreateRequest("시설2", "CODE2", null, null, null, null, null, null)
        )

        // WHEN
        val facilities = facilityService.findAll()

        // THEN
        org.assertj.core.api.Assertions.assertThat<Facility?>(facilities).hasSize(2)
    }

    @Test
    @DisplayName("성공: 시설이 없을 때 findAll 호출 시 빈 리스트를 반환한다")
    fun findAll_whenNoFacilitiesExist_returnsEmptyList() {
        // GIVEN: 데이터가 없는 상태
        facilityRepository!!.deleteAll()

        // WHEN
        val facilities = facilityService!!.findAll()

        // THEN
        org.assertj.core.api.Assertions.assertThat<Facility?>(facilities).isNotNull().isEmpty()
    }

    @Test
    @DisplayName("성공: 유효한 코드로 findByCode 호출 시 시설을 반환한다")
    fun findByCode_withValidCode_returnsFacility() {
        // GIVEN
        val code = "VALID_CODE"
        facilityService!!.save(
            FacilityInstance("시설", code, null, null, null),
            FacilityCreateRequest("시설", code, null, null, null, null, null, null)
        )

        // WHEN
        val found = facilityService.findByCode(code)

        // THEN
        org.assertj.core.api.Assertions.assertThat<Facility?>(found).isNotNull()
        assertThat(found.getCode()).isEqualTo(code)
    }

    @Test
    @DisplayName("실패: 존재하지 않는 코드로 findByCode 호출 시 예외가 발생한다")
    fun findByCode_withNonExistingCode_throwsCustomException() {
        Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { facilityService!!.findByCode("NON_EXISTING") })
    }

    @Test
    @DisplayName("성공: 경로 수정 시 FacilityPathService가 호출된다")
    fun updatePath_delegatesToPathService() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("시설", "CODE", null, null, null),
                FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null)
            )
        val request = FacilityPathUpdateRequest("수정된 경로", "SUB", "{}")

        // WHEN
        facilityService.updatePath(saved.getId(), 1L, request)

        // THEN
        Mockito.verify<FacilityPathService?>(facilityPathService, Mockito.times(1)).update(1L, "수정된 경로", "SUB", "{}")
    }

    @Test
    @DisplayName("성공: 경로 삭제 시 FacilityPathService가 호출된다")
    fun deletePath_delegatesToPathService() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("시설", "CODE", null, null, null),
                FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null)
            )

        // WHEN
        facilityService.deletePath(saved.getId(), 1L)

        // THEN
        Mockito.verify<FacilityPathService?>(facilityPathService, Mockito.times(1)).delete(1L)
    }

    @Test
    @DisplayName("성공: 위치 정보 업데이트 시 좌표와 메타 정보가 변경된다")
    fun updateLocation_updatesPositionCorrectly() {
        // GIVEN
        val saved =
            facilityService!!.save(
                FacilityInstance("시설", "CODE", null, null, null),
                FacilityCreateRequest("시설", "CODE", null, null, null, 1.0, 1.0, null)
            )
        val request =
            FacilityLocationUpdateRequest(127.5, 37.5, "{'new_meta': true}")

        // WHEN
        facilityService.updateLocation(saved.getId(), request)

        // THEN
        val updated = facilityService.findById(saved.getId())
        assertThat(updated.getPosition().getLon()).isEqualTo(127.5)
        assertThat(updated.getPosition().getLat()).isEqualTo(37.5)
        assertThat(updated.getPosition().getLocationMeta()).isEqualTo("{'new_meta': true}")
    }
}
