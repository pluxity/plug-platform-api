package com.pluxity.facility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.pluxity.facility.dto.*;
import com.pluxity.facility.history.FacilityHistoryService;
import com.pluxity.facility.path.FacilityPathService;
import com.pluxity.facility.strategy.FloorService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.util.TestFileUploader;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Collections;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FacilityServiceTest {

    @Autowired private FacilityService facilityService;
    @Autowired private FacilityRepository facilityRepository;
    @Autowired private TestFileUploader testFileUploader;

    @MockitoBean private FacilityHistoryService facilityHistoryService;
    @MockitoBean private FacilityPathService facilityPathService;
    @MockitoBean private FloorService floorService;

    // Facility 추상 클래스를 상속받는 테스트용 구체 클래스
    @Entity
    @DiscriminatorValue("TEST")
    @NoArgsConstructor
    public static class FacilityInstance extends Facility {
        public FacilityInstance(
                String name, String code, String description, Long drawingFileId, Long thumbnailFileId) {
            super(name, code, description, drawingFileId, thumbnailFileId);
        }
    }

    // --- 1. save (생성) 테스트 ---
    @Test
    @DisplayName("성공: 유효한 요청으로 시설 생성 시 모든 필드가 정상적으로 저장된다")
    void save_withValidRequest_savesFacility() {
        // GIVEN
        Long drawingFileId = testFileUploader.initiateTestFileUpload("drawing.dwg");
        Long thumbnailFileId = testFileUploader.initiateTestFileUpload("thumb.png");
        FacilityCreateRequest request =
                new FacilityCreateRequest(
                        "서울역",
                        "SEOUL_ST",
                        "대한민국 수도의 관문",
                        drawingFileId,
                        thumbnailFileId,
                        126.97,
                        37.55,
                        "{'floor': 5}");
        FacilityInstance facility =
                new FacilityInstance(
                        request.name(),
                        request.code(),
                        request.description(),
                        request.drawingFileId(),
                        request.thumbnailFileId());

        // WHEN
        Facility savedFacility = facilityService.save(facility, request);

        // THEN
        assertThat(savedFacility.getId()).isNotNull();
        assertThat(savedFacility.getName()).isEqualTo("서울역");
        assertThat(savedFacility.getCode()).isEqualTo("SEOUL_ST");
        assertThat(savedFacility.getDescription()).isEqualTo("대한민국 수도의 관문");
        assertThat(savedFacility.getDrawingFileId()).isEqualTo(drawingFileId);
        assertThat(savedFacility.getThumbnailFileId()).isEqualTo(thumbnailFileId);
        assertThat(savedFacility.getPosition().getLon()).isEqualTo(126.97);
        assertThat(savedFacility.getPosition().getLat()).isEqualTo(37.55);
        assertThat(savedFacility.getPosition().getLocationMeta()).isEqualTo("{'floor': 5}");

        // Mock 객체 호출 검증
        verify(facilityHistoryService, times(1)).save(drawingFileId, savedFacility.getId(), "최초등록");
    }

    @Test
    @DisplayName("실패: 중복된 코드로 시설 생성 시 예외가 발생한다")
    void save_withDuplicateCode_throwsCustomException() {
        // GIVEN
        facilityService.save(
                new FacilityInstance("시설1", "DUP_CODE", null, null, null),
                new FacilityCreateRequest("시설1", "DUP_CODE", null, null, null, null, null, null));

        FacilityCreateRequest duplicateRequest =
                new FacilityCreateRequest("시설2", "DUP_CODE", null, null, null, null, null, null);
        FacilityInstance facility2 = new FacilityInstance("시설2", "DUP_CODE", null, null, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> facilityService.save(facility2, duplicateRequest));
    }

    // --- 2. find (조회) 테스트 ---
    @Test
    @DisplayName("실패: 존재하지 않는 ID로 조회 시 예외가 발생한다")
    void findById_withNonExistingId_throwsCustomException() {
        assertThrows(CustomException.class, () -> facilityService.findById(9999L));
    }

    // --- 3. update (PATCH 스타일 수정) 테스트 ---
    @Test
    @DisplayName("성공: update 요청 시 일부 필드만 정상적으로 수정된다")
    void update_withPartialRequest_updatesOnlyProvidedFields() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("원본 이름", "ORI_CODE", "원본 설명", null, null),
                        new FacilityCreateRequest("원본 이름", "ORI_CODE", "원본 설명", null, null, 1.0, 1.0, null));

        FacilityUpdateRequest request =
                new FacilityUpdateRequest("수정된 이름", null, null, null, 2.0, null, null);

        // WHEN
        facilityService.update(saved.getId(), request);

        // THEN
        Facility updated = facilityService.findById(saved.getId());
        assertThat(updated.getName()).isEqualTo("수정된 이름"); // 변경된 필드
        assertThat(updated.getPosition().getLon()).isEqualTo(2.0); // 변경된 필드
        assertThat(updated.getCode()).isEqualTo("ORI_CODE"); // 유지된 필드
        assertThat(updated.getDescription()).isEqualTo("원본 설명"); // 유지된 필드
    }

    // --- 4. putUpdate (PUT 스타일 수정) 테스트 ---
    @Test
    @DisplayName("성공: putUpdate 요청 시 모든 필드가 요청대로 덮어쓰기된다 (null 포함)")
    void putUpdate_withFullRequest_overwritesAllFields() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("원본 이름", "ORI_CODE", "원본 설명", null, null),
                        new FacilityCreateRequest("원본 이름", "ORI_CODE", "원본 설명", null, null, 1.0, 1.0, null));

        // description을 null로 하여 덮어쓰기 테스트
        FacilityUpdateRequest request =
                new FacilityUpdateRequest("수정된 이름", "UPD_CODE", null, null, 2.0, 2.0, "{}");

        // WHEN
        facilityService.putUpdate(saved.getId(), request);

        // THEN
        Facility updated = facilityService.findById(saved.getId());
        assertThat(updated.getName()).isEqualTo("수정된 이름");
        assertThat(updated.getCode()).isEqualTo("UPD_CODE");
        assertThat(updated.getDescription()).isNull(); // null로 덮어쓰기 되었는지 확인
        assertThat(updated.getPosition().getLat()).isEqualTo(2.0);
    }

    // --- 5. delete (삭제) 테스트 ---
    @Test
    @DisplayName("성공: 시설 삭제 시 DB에서 소프트 삭제된다")
    void deleteFacility_withExistingId_softDeletesFacility() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("삭제될 시설", "DEL_CODE", null, null, null),
                        new FacilityCreateRequest("삭제될 시설", "DEL_CODE", null, null, null, null, null, null));

        // WHEN
        facilityService.deleteFacility(saved.getId());

        // THEN
        // SoftDelete 이므로 findById는 예외를 던져야 함
        assertThrows(CustomException.class, () -> facilityService.findById(saved.getId()));
        // Repository 레벨에서는 여전히 존재해야 함 (필요 시 네이티브 쿼리 등으로 확인 가능)
    }

    // --- 6. 도면/경로/위치/층 등 서브 도메인 관련 메서드 테스트 ---
    @Test
    @DisplayName("성공: 도면 파일 업데이트 시 히스토리가 기록된다")
    void updateDrawingFile_updatesFileAndSavesHistory() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("시설", "CODE", null, null, null),
                        new FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null));
        Long newDrawingFileId = testFileUploader.initiateTestFileUpload("new_drawing.dwg");
        FacilityDrawingUpdateRequest request =
                new FacilityDrawingUpdateRequest(newDrawingFileId, "도면 교체");

        // WHEN
        facilityService.updateDrawingFile(saved.getId(), request);

        // THEN
        Facility updated = facilityService.findById(saved.getId());
        assertThat(updated.getDrawingFileId()).isEqualTo(newDrawingFileId);

        verify(facilityHistoryService, times(1)).save(newDrawingFileId, saved.getId(), "도면 교체");
    }

    @Test
    @DisplayName("성공: 경로 저장 시 FacilityPathService가 호출된다")
    void savePath_delegatesToPathService() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("시설", "CODE", null, null, null),
                        new FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null));
        FacilityPathSaveRequest request = new FacilityPathSaveRequest("주 경로", "MAIN", "{}");

        // WHEN
        facilityService.savePath(saved.getId(), request);

        // THEN
        verify(facilityPathService, times(1))
                .save(any(Facility.class), eq("주 경로"), eq("MAIN"), eq("{}"));
    }

    @Test
    @DisplayName("성공: 층 정보 업데이트 시 FloorService가 호출된다")
    void updateFloor_delegatesToFloorService() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("시설", "CODE", null, null, null),
                        new FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null));
        FacilityFloorUpdateRequest request = new FacilityFloorUpdateRequest(Collections.emptyList());

        // WHEN
        facilityService.updateFloor(saved.getId(), request);

        // THEN
        verify(floorService, times(1)).update(any(Facility.class), anyList());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 시설의 경로 저장 시 예외가 발생한다")
    void savePath_onNonExistingFacility_throwsCustomException() {
        // GIVEN
        FacilityPathSaveRequest request = new FacilityPathSaveRequest("주 경로", "MAIN", "{}");

        // WHEN & THEN
        assertThrows(CustomException.class, () -> facilityService.savePath(9999L, request));
    }

    @Test
    @DisplayName("성공: 일부 선택적 필드가 null일 때도 시설 생성이 성공한다")
    void save_withNullOptionalFields_succeeds() {
        // GIVEN: code, description, files, location 정보가 모두 null인 요청
        FacilityCreateRequest request =
                new FacilityCreateRequest("필수 필드만 있는 시설", "MANDATORY", null, null, null, null, null, null);
        FacilityInstance facility =
                new FacilityInstance(request.name(), request.code(), null, null, null);

        // WHEN
        Facility savedFacility = facilityService.save(facility, request);

        // THEN
        assertThat(savedFacility).isNotNull();
        assertThat(savedFacility.getName()).isEqualTo("필수 필드만 있는 시설");
        assertThat(savedFacility.getCode()).isEqualTo("MANDATORY");
        assertThat(savedFacility.getDescription()).isNull();
        assertThat(savedFacility.getDrawingFileId()).isNull();
        assertThat(savedFacility.getPosition()).isNotNull(); // Embedded 객체는 생성됨
    }

    @Test
    @DisplayName("실패: update 시 다른 시설이 사용 중인 코드로 변경하면 예외가 발생한다")
    void update_withExistingCodeOfAnotherFacility_throwsCustomException() {
        // GIVEN: 두 개의 시설 생성
        facilityService.save(
                new FacilityInstance("시설1", "CODE1", null, null, null),
                new FacilityCreateRequest("시설1", "CODE1", null, null, null, null, null, null));
        Facility saved2 =
                facilityService.save(
                        new FacilityInstance("시설2", "CODE2", null, null, null),
                        new FacilityCreateRequest("시설2", "CODE2", null, null, null, null, null, null));

        // WHEN & THEN: 시설2의 코드를 시설1의 코드로 변경 시도
        FacilityUpdateRequest request =
                new FacilityUpdateRequest(null, "CODE1", null, null, null, null, null);
        assertThrows(CustomException.class, () -> facilityService.update(saved2.getId(), request));
    }

    @Test
    @DisplayName("성공: findAll 호출 시 모든 시설 목록을 반환한다")
    void findAll_whenFacilitiesExist_returnsListOfFacilities() {
        // GIVEN
        facilityService.save(
                new FacilityInstance("시설1", "CODE1", null, null, null),
                new FacilityCreateRequest("시설1", "CODE1", null, null, null, null, null, null));
        facilityService.save(
                new FacilityInstance("시설2", "CODE2", null, null, null),
                new FacilityCreateRequest("시설2", "CODE2", null, null, null, null, null, null));

        // WHEN
        var facilities = facilityService.findAll();

        // THEN
        assertThat(facilities).hasSize(2);
    }

    @Test
    @DisplayName("성공: 시설이 없을 때 findAll 호출 시 빈 리스트를 반환한다")
    void findAll_whenNoFacilitiesExist_returnsEmptyList() {
        // GIVEN: 데이터가 없는 상태
        facilityRepository.deleteAll();

        // WHEN
        var facilities = facilityService.findAll();

        // THEN
        assertThat(facilities).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("성공: 유효한 코드로 findByCode 호출 시 시설을 반환한다")
    void findByCode_withValidCode_returnsFacility() {
        // GIVEN
        String code = "VALID_CODE";
        facilityService.save(
                new FacilityInstance("시설", code, null, null, null),
                new FacilityCreateRequest("시설", code, null, null, null, null, null, null));

        // WHEN
        Facility found = facilityService.findByCode(code);

        // THEN
        assertThat(found).isNotNull();
        assertThat(found.getCode()).isEqualTo(code);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 코드로 findByCode 호출 시 예외가 발생한다")
    void findByCode_withNonExistingCode_throwsCustomException() {
        assertThrows(CustomException.class, () -> facilityService.findByCode("NON_EXISTING"));
    }

    @Test
    @DisplayName("성공: 경로 수정 시 FacilityPathService가 호출된다")
    void updatePath_delegatesToPathService() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("시설", "CODE", null, null, null),
                        new FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null));
        FacilityPathUpdateRequest request = new FacilityPathUpdateRequest("수정된 경로", "SUB", "{}");

        // WHEN
        facilityService.updatePath(saved.getId(), 1L, request);

        // THEN
        verify(facilityPathService, times(1)).update(1L, "수정된 경로", "SUB", "{}");
    }

    @Test
    @DisplayName("성공: 경로 삭제 시 FacilityPathService가 호출된다")
    void deletePath_delegatesToPathService() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("시설", "CODE", null, null, null),
                        new FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null));

        // WHEN
        facilityService.deletePath(saved.getId(), 1L);

        // THEN
        verify(facilityPathService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("성공: 위치 정보 업데이트 시 좌표와 메타 정보가 변경된다")
    void updateLocation_updatesPositionCorrectly() {
        // GIVEN
        Facility saved =
                facilityService.save(
                        new FacilityInstance("시설", "CODE", null, null, null),
                        new FacilityCreateRequest("시설", "CODE", null, null, null, 1.0, 1.0, null));
        FacilityLocationUpdateRequest request =
                new FacilityLocationUpdateRequest(127.5, 37.5, "{'new_meta': true}");

        // WHEN
        facilityService.updateLocation(saved.getId(), request);

        // THEN
        Facility updated = facilityService.findById(saved.getId());
        assertThat(updated.getPosition().getLon()).isEqualTo(127.5);
        assertThat(updated.getPosition().getLat()).isEqualTo(37.5);
        assertThat(updated.getPosition().getLocationMeta()).isEqualTo("{'new_meta': true}");
    }
}
