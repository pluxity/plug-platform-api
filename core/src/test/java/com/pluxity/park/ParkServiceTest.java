package com.pluxity.park;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.facility.Facility;
import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.file.constant.FileStatus;
import com.pluxity.global.exception.CustomException;
import com.pluxity.park.dto.ParkCreateRequest;
import com.pluxity.park.dto.ParkResponse;
import com.pluxity.park.dto.ParkUpdateRequest;
import com.pluxity.util.TestFileUploader;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ParkServiceTest {

    @Autowired private ParkService parkService;
    @Autowired private ParkRepository parkRepository;
    @Autowired private TestFileUploader testFileUploader;

    private static final String BOUNDARY_JSON_1 =
            "{\"type\":\"Polygon\",\"coordinates\":[[[127.0,37.5],[127.1,37.5],[127.1,37.6],[127.0,37.6],[127.0,37.5]]]}";
    private static final String BOUNDARY_JSON_2 =
            "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}";

    // --- Create Test ---

    @Test
    @DisplayName("성공: 모든 필드를 포함한 유효한 요청으로 공원을 생성하고, 모든 응답 필드와 DB 상태를 상세히 검증한다")
    void save_WithValidRequest_SavesParkAndReturnsDetailedResponse() {
        // GIVEN: 공원 생성에 필요한 모든 데이터 준비
        Long drawingFileId = testFileUploader.initiateTestFileUpload("park_drawing.dwg");
        Long thumbnailFileId = testFileUploader.initiateTestFileUpload("park_thumbnail.png");
        ParkCreateRequest request =
                new ParkCreateRequest(
                        new FacilityCreateRequest(
                                "플럭시티 공원",
                                "PARK_001",
                                "도심 속의 쉼터",
                                drawingFileId,
                                thumbnailFileId,
                                126.9780,
                                37.5665,
                                "{\"area\":\"15,000sqm\"}"),
                        BOUNDARY_JSON_1);

        // WHEN: 공원 생성
        Long createdParkId = parkService.save(request);

        // THEN: 응답 DTO 검증
        ParkResponse response = parkService.findById(createdParkId);
        assertThat(response.facility().id()).isEqualTo(createdParkId);
        assertThat(response.facility().name()).isEqualTo("플럭시티 공원");
        assertThat(response.facility().code()).isEqualTo("PARK_001");
        assertThat(response.facility().description()).isEqualTo("도심 속의 쉼터");
        assertThat(response.boundary()).isEqualTo(BOUNDARY_JSON_1);

        // 파일 정보 검증
        assertThat(response.facility().drawing().id()).isEqualTo(drawingFileId);
        assertThat(response.facility().drawing().fileStatus()).isEqualTo(FileStatus.COMPLETE.name());
        assertThat(response.facility().thumbnail().id()).isEqualTo(thumbnailFileId);

        // 위치 정보 검증
        assertThat(response.facility().lon()).isEqualTo(126.9780);
        assertThat(response.facility().lat()).isEqualTo(37.5665);
        assertThat(response.facility().locationMeta()).isEqualTo("{\"area\":\"15,000sqm\"}");

        // THEN: 데이터베이스 최종 상태 직접 검증
        Park savedPark = parkRepository.findById(createdParkId).orElseThrow();
        assertThat(savedPark.getName()).isEqualTo("플럭시티 공원");
        assertThat(savedPark.getBoundary()).isEqualTo(BOUNDARY_JSON_1);
        assertThat(savedPark.getDrawingFileId()).isEqualTo(drawingFileId);
    }

    @Test
    @DisplayName("성공: 선택적 필드(설명, 파일, 경계 등)가 null일 때 공원 생성이 성공한다")
    void save_WithNullOptionalFields_Succeeds() {
        // GIVEN: 필수 필드만 채운 요청
        ParkCreateRequest request =
                new ParkCreateRequest(
                        new FacilityCreateRequest("필수 공원", "PARK_REQ", null, null, null, null, null, null),
                        null // boundary 정보 없음
                        );

        // WHEN
        Long createdParkId = parkService.save(request);

        // THEN: 생성된 공원 정보 검증
        ParkResponse response = parkService.findById(createdParkId);
        assertThat(response.facility().name()).isEqualTo("필수 공원");
        assertThat(response.facility().code()).isEqualTo("PARK_REQ");
        assertThat(response.facility().description()).isNull();
        assertThat(response.facility().drawing().id()).isNull();
        assertThat(response.facility().thumbnail().id()).isNull();
        assertThat(response.boundary()).isNull();
    }

    @Test
    @DisplayName("실패: 중복된 코드로 공원 생성 시 예외가 발생한다")
    void save_WithDuplicateCode_ThrowsCustomException() {
        // GIVEN: 기준 공원 생성
        createAndSavePark("기준 공원", "DUPE_CODE");
        ParkCreateRequest duplicateRequest =
                new ParkCreateRequest(
                        new FacilityCreateRequest("다른 이름 공원", "DUPE_CODE", null, null, null, null, null, null),
                        null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> parkService.save(duplicateRequest));
    }

    // --- Read Test ---

    @Test
    @DisplayName("성공: 모든 공원 조회 시 상세 정보가 포함된 목록을 반환한다")
    void findAll_ReturnsListOfDetailedParkResponses() {
        // GIVEN: 2개의 서로 다른 공원 생성
        createAndSavePark("공원 A", "PARK_A");
        createAndSavePark("공원 B", "PARK_B");

        // WHEN
        List<ParkResponse> responses = parkService.findAll();

        // THEN
        assertThat(responses).hasSize(2);
        ParkResponse parkA =
                responses.stream()
                        .filter(p -> p.facility().code().equals("PARK_A"))
                        .findFirst()
                        .orElseThrow();
        assertThat(parkA.facility().name()).isEqualTo("공원 A");
    }

    @Test
    @DisplayName("성공: 공원이 없는 경우 전체 조회 시 빈 리스트를 반환한다")
    void findAll_WhenNoParksExist_ReturnsEmptyList() {
        // GIVEN: 데이터 없음
        // WHEN
        List<ParkResponse> responses = parkService.findAll();
        // THEN
        assertThat(responses).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 공원 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // GIVEN
        Long nonExistingId = 9999L;
        // WHEN & THEN
        assertThrows(CustomException.class, () -> parkService.findById(nonExistingId));
    }

    // --- Update Test ---

    @Test
    @DisplayName("성공(PUT): 모든 필드를 교체하는 수정 요청 시, null 필드는 null로 반영된다")
    void update_FullReplace_ReplacesAllFields() {
        // GIVEN: 원본 데이터 생성
        Long parkId = createAndSavePark("원본 공원", "PARK_ORI");
        Long newThumbnailId = testFileUploader.initiateTestFileUpload("new_thumbnail.dwg");

        // GIVEN: thumbnail만 변경하는 PUT 요청
        ParkUpdateRequest updateRequest =
                new ParkUpdateRequest(
                        new FacilityUpdateRequest("수정된 공원", "PARK_UPD", null, newThumbnailId, null, null, null),
                        BOUNDARY_JSON_2);

        // WHEN
        parkService.update(parkId, updateRequest);

        // THEN: 응답 DTO 및 DB 상태 검증
        ParkResponse response = parkService.findById(parkId);
        assertThat(response.facility().name()).isEqualTo("수정된 공원");
        assertThat(response.facility().description()).isNull();
        assertThat(response.facility().drawing().id()).isNull();
        assertThat(response.facility().thumbnail().id()).isEqualTo(newThumbnailId);
        assertThat(response.boundary()).isEqualTo(BOUNDARY_JSON_2);

        Facility updatedFacilityInDb = parkRepository.findById(parkId).orElseThrow();
        assertThat(updatedFacilityInDb.getDescription()).isNull();
    }

    @Test
    @DisplayName("성공: 경계(Boundary) 정보만 수정해도 정상적으로 반영된다")
    void update_OnlyBoundary_UpdatesSuccessfully() {
        // GIVEN: 원본 데이터 생성
        Long parkId = createAndSavePark("경계 테스트 공원", "PARK_BOUND");
        Park originalPark = parkRepository.findById(parkId).orElseThrow();

        // GIVEN: Facility 정보는 그대로 두고, boundary 정보만 변경하는 요청
        ParkUpdateRequest updateRequest =
                new ParkUpdateRequest(
                        new FacilityUpdateRequest(
                                originalPark.getName(),
                                originalPark.getCode(),
                                originalPark.getDescription(),
                                null,
                                null,
                                null,
                                null),
                        BOUNDARY_JSON_2);

        // WHEN
        parkService.update(parkId, updateRequest);

        // THEN
        ParkResponse response = parkService.findById(parkId);
        assertThat(response.facility().name()).isEqualTo("경계 테스트 공원"); // 유지됨
        assertThat(response.boundary()).isEqualTo(BOUNDARY_JSON_2); // 변경됨
    }

    // --- Delete Test ---

    @Test
    @DisplayName("성공: 공원을 삭제하면 연관된 시설 정보와 함께 DB에서 삭제된다")
    void delete_RemovesParkAndFacility() {
        // GIVEN
        Long parkId = createAndSavePark("삭제될 공원", "PARK_DEL");
        assertThat(parkRepository.findById(parkId)).isPresent();

        // WHEN
        parkService.delete(parkId);

        // THEN
        assertThat(parkRepository.findById(parkId)).isEmpty();
        assertThrows(CustomException.class, () -> parkService.findById(parkId));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 삭제 요청 시 예외가 발생한다")
    void delete_WithNonExistingId_ThrowsCustomException() {
        // GIVEN
        Long nonExistingId = 9998L;
        // WHEN & THEN
        assertThrows(CustomException.class, () -> parkService.delete(nonExistingId));
    }

    // --- Helper Methods ---

    private Long createAndSavePark(String name, String code) {
        ParkCreateRequest request =
                new ParkCreateRequest(
                        new FacilityCreateRequest(name, code, "설명", null, null, null, null, null),
                        BOUNDARY_JSON_1);
        return parkService.save(request);
    }
}
