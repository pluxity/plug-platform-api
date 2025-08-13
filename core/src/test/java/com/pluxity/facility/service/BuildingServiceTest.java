package com.pluxity.facility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.building.BuildingRepository;
import com.pluxity.building.BuildingService;
import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingResponse;
import com.pluxity.building.dto.BuildingUpdateRequest;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.file.constant.FileStatus;
import com.pluxity.global.exception.CustomException;
import com.pluxity.util.TestFileUploader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BuildingServiceTest {

    @Autowired
    BuildingService buildingService;

    @Autowired
    BuildingRepository buildingRepository;

    @Autowired
    FacilityService facilityService;

    @Autowired
    TestFileUploader testFileUploader;

    private Long drawingFileId;
    private Long thumbnailFileId;
    private BuildingCreateRequest createRequest;


    @BeforeEach
    void setUp() { // throws IOException 제거
        drawingFileId = testFileUploader.initiateTestFileUpload("drawing.png");
        thumbnailFileId = testFileUploader.initiateTestFileUpload("thumbnail.png");

        // 테스트 데이터 준비
        FacilityCreateRequest facilityRequest = new FacilityCreateRequest(
                "테스트 건물",
                "AAA",
                "테스트 건물 설명",
                drawingFileId,
                thumbnailFileId
                , null
                , null
                , null
        );

        List<FloorRequest> floorRequests = new ArrayList<>();
        floorRequests.add(new FloorRequest(
                "1층",
                "1"
        ));

        createRequest = new BuildingCreateRequest(
                facilityRequest,
                floorRequests
        );
    }

    @Test
    @DisplayName("유효한 요청으로 건물 생성 시 건물과 층이 저장된다")
    void save_WithValidRequest_SavesBuildingAndFloors() {
        // when
        Long id = buildingService.save(createRequest);

        // then
        assertThat(id).isNotNull();

        // 저장된 건물 확인
        BuildingResponse savedBuilding = buildingService.findById(id);
        assertThat(savedBuilding).isNotNull();
        assertThat(savedBuilding.facility().id()).isEqualTo(id);
        assertThat(savedBuilding.facility().code()).isEqualTo("AAA");
        assertThat(savedBuilding.facility().name()).isEqualTo("테스트 건물");
        assertThat(savedBuilding.facility().description()).isEqualTo("테스트 건물 설명");

        assertThat(savedBuilding.facility().drawing().id()).isNotNull();
        assertThat(savedBuilding.facility().drawing().url()).isNotNull();
        assertThat(savedBuilding.facility().drawing().originalFileName()).isNotNull();
        assertThat(savedBuilding.facility().drawing().contentType()).isNotNull();
        assertThat(savedBuilding.facility().drawing().fileStatus()).isEqualTo(FileStatus.COMPLETE.name());

        assertThat(savedBuilding.facility().thumbnail().id()).isNotNull();
        assertThat(savedBuilding.facility().thumbnail().url()).isNotNull();
        assertThat(savedBuilding.facility().thumbnail().originalFileName()).isNotNull();
        assertThat(savedBuilding.facility().thumbnail().contentType()).isNotNull();
        assertThat(savedBuilding.facility().thumbnail().fileStatus()).isEqualTo(FileStatus.COMPLETE.name());

        assertThat(savedBuilding.facility().paths()).isEmpty();
        assertThat(savedBuilding.facility().lon()).isNull();
        assertThat(savedBuilding.facility().lat()).isNull();
        assertThat(savedBuilding.facility().locationMeta()).isNull();

        assertThat(savedBuilding.floors()).isNotEmpty();
        assertThat(savedBuilding.floors().getFirst().name()).isEqualTo("1층");
        assertThat(savedBuilding.floors().getFirst().floorId()).isEqualTo("1");
    }

    @Test
    @DisplayName("모든 건물 조회 시 건물 목록이 반환된다")
    void findAll_ReturnsListOfBuildingResponses() {
        // given
        buildingService.save(createRequest);

        // when
        List<BuildingResponse> responses = buildingService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.get(0).facility().name()).isEqualTo("테스트 건물");
        assertThat(responses.get(0).facility().description()).isEqualTo("테스트 건물 설명");
    }

    @Test
    @DisplayName("ID로 건물 조회 시 건물 정보가 반환된다")
    void findById_WithExistingId_ReturnsBuildingResponse() {
        // given
        Long id = buildingService.save(createRequest);

        // when
        BuildingResponse response = buildingService.findById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.facility().name()).isEqualTo("테스트 건물");
        assertThat(response.facility().description()).isEqualTo("테스트 건물 설명");
        assertThat(response.floors()).isNotEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 건물 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> buildingService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 건물 정보 수정 시 건물 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesBuilding() {
        // given
        Long id = buildingService.save(createRequest);
        BuildingUpdateRequest updateRequest = new BuildingUpdateRequest(
                new FacilityUpdateRequest(
                        "수정된 건물 이름",
                        "수정된 코드",
                        "수정된 건물 설명",
                        null
                        , null
                        , null
                        , null
                ), null
        );

        // when
        buildingService.update(id, updateRequest);

        // then
        Facility updatedBuilding = facilityService.findById(id);
        assertThat(updatedBuilding.getName()).isEqualTo("수정된 건물 이름");
        assertThat(updatedBuilding.getDescription()).isEqualTo("수정된 건물 설명");
    }

    @Test
    @DisplayName("건물 삭제 시 모든 이력이 삭제된다")
    void delete() {
        // given
        Long id = buildingService.save(createRequest);

        // when
        BuildingResponse response = buildingService.findById(id);
        assertThat(response).isNotNull();

        // then
        buildingService.delete(id);

        // 삭제 후에는 해당 ID로 건물을 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> buildingService.findById(id));
    }

    @Test
    @DisplayName("선택 필드가 null이어도 건물 생성이 성공한다")
    void save_WithNullOptionalFields_Succeeds() {
        // given
        var facilityRequest = new FacilityCreateRequest(
                "옵션없음", // name (required)
                "BBB",      // code (required)
                null,        // description optional
                null,        // drawingFileId optional
                null,        // thumbnailFileId optional
                null,        // lon optional
                null,        // lat optional
                null         // locationMeta optional
        );

        var create = new BuildingCreateRequest(
                facilityRequest,
                List.of() // floors empty
        );

        // when
        Long id = buildingService.save(create);

        // then
        assertThat(id).isNotNull();
        BuildingResponse response = buildingService.findById(id);
        assertThat(response.facility().name()).isEqualTo("옵션없음");
        assertThat(response.facility().code()).isEqualTo("BBB");
        assertThat(response.facility().description()).isNull();
        assertThat(response.floors()).isEmpty();
    }

    @Test
    @DisplayName("데이터가 없을 때 findAll 은 빈 리스트를 반환한다")
    void findAll_WhenEmpty_ReturnsEmptyList() {
        // when
        List<BuildingResponse> responses = buildingService.findAll();

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("부분 업데이트: 이름만 변경")
    void update_Partial_OnlyName() {
        // given
        Long id = buildingService.save(createRequest);
        String originalDescription = buildingService.findById(id).facility().description();

        var request = new BuildingUpdateRequest(
                new FacilityUpdateRequest(
                        "부분업데이트이름", // name 변경
                        null,               // code 유지
                        null,               // description 유지
                        null,               // thumbnail 유지
                        null,               // lon 유지
                        null,               // lat 유지
                        null                // locationMeta 유지
                ),
                null // floors 변경 없음
        );

        // when
        buildingService.update(id, request);

        // then
        var updated = facilityService.findById(id);
        assertThat(updated.getName()).isEqualTo("부분업데이트이름");
        assertThat(updated.getDescription()).isEqualTo(originalDescription);
    }

    @Test
    @DisplayName("필드를 null 로 업데이트하여도 유지 (Patch)")
    void update_SetFieldToNull() {
        // given
        Long id = buildingService.save(createRequest);

        var request = new BuildingUpdateRequest(
                new FacilityUpdateRequest(
                        null,   // name 유지
                        null,   // code 유지
                        null,   // description 을 null 로 (이미 null 일 수 있으므로 이후 no-op 과 구분 위해 먼저 값 설정)
                        null,
                        null,
                        null,
                        null
                ),
                null
        );

        // when
        buildingService.update(id, request);

        // then
        var updated = facilityService.findById(id);
        assertThat(updated.getDescription()).isEqualTo(createRequest.facility().description());
    }

    @Test
    @DisplayName("No-Op 업데이트: 동일 데이터로 업데이트 시 변경 없음")
    void update_NoOp_DoesNotChangeData() {
        // given
        Long id = buildingService.save(createRequest);
        var before = facilityService.findById(id);

        var request = new BuildingUpdateRequest(
                new FacilityUpdateRequest(
                        before.getName(),
                        before.getCode(),
                        before.getDescription(),
                        before.getThumbnailFileId(),
                        before.getPosition() != null ? before.getPosition().getLon() : null,
                        before.getPosition() != null ? before.getPosition().getLat() : null,
                        before.getPosition() != null ? before.getPosition().getLocationMeta() : null
                ),
                null
        );

        // when
        buildingService.update(id, request);

        // then
        var after = facilityService.findById(id);
        assertThat(after.getName()).isEqualTo(before.getName());
        assertThat(after.getCode()).isEqualTo(before.getCode());
        assertThat(after.getDescription()).isEqualTo(before.getDescription());
    }

    @Test
    @DisplayName("PUT 업데이트: 모든 필드를 교체")
    void putUpdate_FullUpdate_ReplacesFields() {
        // given
        Long id = buildingService.save(createRequest);

        var putRequest = new BuildingUpdateRequest(
                new FacilityUpdateRequest(
                        "풀업데이트이름",
                        "CODE2",
                        "풀업데이트설명",
                        null, // 썸네일 제거
                        120.0,
                        30.0,
                        "{\"height\":10}"
                ),
                List.of() // floors 를 비워서 반영
        );

        // when
        buildingService.putUpdate(id, putRequest);

        // then
        var updated = facilityService.findById(id);
        assertThat(updated.getName()).isEqualTo("풀업데이트이름");
        assertThat(updated.getCode()).isEqualTo("CODE2");
        assertThat(updated.getDescription()).isEqualTo("풀업데이트설명");
        assertThat(buildingService.findById(id).floors()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID 삭제 시 예외 발생")
    void delete_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 987654321L;

        // when & then
        assertThrows(CustomException.class, () -> buildingService.delete(nonExistingId));
    }

    @Test
    @DisplayName("동일한 코드로 두 번째 건물 생성 시 중복 예외 발생")
    void save_WithDuplicateCode_ThrowsCustomException() {
        // given
        buildingService.save(createRequest);

        var dupFacilityRequest = new FacilityCreateRequest(
                "다른이름",
                "AAA", // same code as in setUp
                "설명",
                null,
                null,
                null,
                null,
                null
        );
        var dupCreate = new BuildingCreateRequest(dupFacilityRequest, List.of());

        // when & then
        assertThrows(CustomException.class, () -> buildingService.save(dupCreate));
    }

    @Test
    @DisplayName("다른 건물의 코드로 업데이트 시 중복 예외 발생")
    void update_ToDuplicateCode_ThrowsCustomException() {
        // given
        buildingService.save(createRequest); // code AAA

        var facilityRequest2 = new FacilityCreateRequest(
                "두번째",
                "BBB",
                "desc",
                null, null, null, null, null
        );
        Long secondId = buildingService.save(new BuildingCreateRequest(facilityRequest2, List.of()));

        var updateToDup = new BuildingUpdateRequest(
                new FacilityUpdateRequest(
                        null,
                        "AAA", // duplicate code
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                null
        );

        // when & then
        assertThrows(CustomException.class, () -> buildingService.update(secondId, updateToDup));
    }

    @Test
    @DisplayName("층 정보를 빈 리스트로 업데이트하면 모든 층이 제거된다")
    void update_FloorsToEmpty_RemovesAllFloors() {
        // given
        Long id = buildingService.save(createRequest); // has one floor
        var clearFloors = new BuildingUpdateRequest(
                new FacilityUpdateRequest(null, null, null, null, null, null, null),
                List.of()
        );

        // when
        buildingService.update(id, clearFloors);

        // then
        assertThat(buildingService.findById(id).floors()).isEmpty();
    }
}