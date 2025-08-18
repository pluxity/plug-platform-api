package com.pluxity.facility.service

import com.pluxity.building.BuildingService
import com.pluxity.building.dto.BuildingCreateRequest
import com.pluxity.building.dto.BuildingResponse
import com.pluxity.building.dto.BuildingUpdateRequest
import com.pluxity.facility.FacilityService
import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.facility.dto.FacilityUpdateRequest
import com.pluxity.facility.floor.dto.FloorRequest
import com.pluxity.file.constant.FileStatus
import com.pluxity.global.exception.CustomException
import com.pluxity.util.TestFileUploader
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
internal class BuildingServiceTest {
    @Autowired
    lateinit var buildingService: BuildingService

    @Autowired
    lateinit var facilityService: FacilityService

    @Autowired
    lateinit var testFileUploader: TestFileUploader

    private var drawingFileId: Long? = null
    private var thumbnailFileId: Long? = null
    private lateinit var createRequest: BuildingCreateRequest

    @BeforeEach
    fun setUp() { // throws IOException 제거
        drawingFileId = testFileUploader.initiateTestFileUpload("drawing.png")
        thumbnailFileId = testFileUploader.initiateTestFileUpload("thumbnail.png")

        // 테스트 데이터 준비
        val facilityRequest =
            FacilityCreateRequest(
                "테스트 건물",
                "AAA",
                "테스트 건물 설명",
                drawingFileId,
                thumbnailFileId,
                null,
                null,
                null,
            )

        val floorRequests: List<FloorRequest> = listOf(FloorRequest("1층", "1"))

        createRequest = BuildingCreateRequest(facilityRequest, floorRequests)
    }

    @Test
    @DisplayName("유효한 요청으로 건물 생성 시 건물과 층이 저장된다")
    fun save_WithValidRequest_SavesBuildingAndFloors() {
        // when
        val id = buildingService.save(createRequest)

        // then
        assertThat(id).isNotNull()

        // 저장된 건물 확인
        val savedBuilding = buildingService.findById(id)
        assertThat(savedBuilding).isNotNull()
        assertThat(savedBuilding.facility.id).isEqualTo(id)
        assertThat(savedBuilding.facility.code).isEqualTo("AAA")
        assertThat(savedBuilding.facility.name).isEqualTo("테스트 건물")
        assertThat(savedBuilding.facility.description).isEqualTo("테스트 건물 설명")

        assertThat(savedBuilding.facility.drawing.id).isNotNull()
        assertThat(savedBuilding.facility.drawing.url).isNotNull()
        assertThat(savedBuilding.facility.drawing.originalFileName).isNotNull()
        assertThat(savedBuilding.facility.drawing.contentType).isNotNull()
        assertThat(savedBuilding.facility.drawing.fileStatus)
            .isEqualTo(FileStatus.COMPLETE.name)

        assertThat(savedBuilding.facility.thumbnail.id).isNotNull()
        assertThat(savedBuilding.facility.thumbnail.url).isNotNull()
        assertThat(savedBuilding.facility.thumbnail.originalFileName).isNotNull()
        assertThat(savedBuilding.facility.thumbnail.contentType).isNotNull()
        assertThat(savedBuilding.facility.thumbnail.fileStatus)
            .isEqualTo(FileStatus.COMPLETE.name)

        assertThat(savedBuilding.facility.paths).isEmpty()
        assertThat(savedBuilding.facility.lon).isNull()
        assertThat(savedBuilding.facility.lat).isNull()
        assertThat(savedBuilding.facility.locationMeta).isNull()

        assertThat(savedBuilding.floors).isNotEmpty()
        assertThat(savedBuilding.floors?.first()?.name).isEqualTo("1층")
        assertThat(savedBuilding.floors?.first()?.floorId).isEqualTo("1")
    }

    @Test
    @DisplayName("모든 건물 조회 시 건물 목록이 반환된다")
    fun findAll_ReturnsListOfBuildingResponses() {
        // given
        buildingService.save(createRequest)

        // when
        val responses: List<BuildingResponse> = buildingService.findAll()

        // then
        assertThat(responses).isNotEmpty()
        assertThat(responses.first().facility.name).isEqualTo("테스트 건물")
        assertThat(responses.first().facility.description).isEqualTo("테스트 건물 설명")
    }

    @Test
    @DisplayName("ID로 건물 조회 시 건물 정보가 반환된다")
    fun findById_WithExistingId_ReturnsBuildingResponse() {
        // given
        val id = buildingService.save(createRequest)

        // when
        val response = buildingService.findById(id)

        // then
        assertThat(response).isNotNull()
        assertThat(response.facility.name).isEqualTo("테스트 건물")
        assertThat(response.facility.description).isEqualTo("테스트 건물 설명")
        assertThat(response.floors).isNotEmpty()
    }

    @Test
    @DisplayName("존재하지 않는 ID로 건물 조회 시 예외가 발생한다")
    fun findById_WithNonExistingId_ThrowsCustomException() {
        // given
        val nonExistingId = 9999L

        // when & then
        assertThatThrownBy {
            buildingService.findById(nonExistingId)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("유효한 요청으로 건물 정보 수정 시 건물 정보가 업데이트된다")
    fun update_WithValidRequest_UpdatesBuilding() {
        // given
        val id = buildingService.save(createRequest)
        val updateRequest =
            BuildingUpdateRequest(
                FacilityUpdateRequest("수정된 건물 이름", "수정된 코드", "수정된 건물 설명", null, null, null, null),
                listOf(),
            )

        // when
        buildingService.update(id, updateRequest)

        // then
        val updatedBuilding = facilityService.findById(id)
        assertThat(updatedBuilding.name).isEqualTo("수정된 건물 이름")
        assertThat(updatedBuilding.description).isEqualTo("수정된 건물 설명")
    }

    @Test
    @DisplayName("건물 삭제 시 모든 이력이 삭제된다")
    fun delete() {
        // given
        val id = buildingService.save(createRequest)

        // when
        val response = buildingService.findById(id)
        assertThat(response).isNotNull()

        // then
        buildingService.delete(id)

        // 삭제 후에는 해당 ID로 건물을 찾을 수 없어야 함
        assertThatThrownBy {
            buildingService.findById(id)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("선택 필드가 null이어도 건물 생성이 성공한다")
    fun save_WithNullOptionalFields_Succeeds() {
        // given
        val facilityRequest =
            FacilityCreateRequest(
                // name (required)
                "옵션없음",
                // code (required)
                "BBB",
                // description optional
                null,
                // drawingFileId optional
                null,
                // thumbnailFileId optional
                null,
                // lon optional
                null,
                // lat optional
                null,
                // locationMeta optional
                null,
            )

        val create =
            BuildingCreateRequest(
                // floors empty
                facilityRequest,
                mutableListOf(),
            )

        // when
        val id = buildingService.save(create)

        // then
        assertThat(id).isNotNull()
        val response = buildingService.findById(id)
        assertThat(response.facility.name).isEqualTo("옵션없음")
        assertThat(response.facility.code).isEqualTo("BBB")
        assertThat(response.facility.description).isNull()
        assertThat(response.floors).isEmpty()
    }

    @Test
    @DisplayName("데이터가 없을 때 findAll 은 빈 리스트를 반환한다")
    fun findAll_WhenEmpty_ReturnsEmptyList() {
        // when
        val responses: List<BuildingResponse> = buildingService.findAll()

        // then
        assertThat(responses).isEmpty()
    }

    @Test
    @DisplayName("부분 업데이트: 이름만 변경")
    fun update_Partial_OnlyName() {
        // given
        val id = buildingService.save(createRequest)
        val originalDescription = buildingService.findById(id).facility.description

        val request =
            BuildingUpdateRequest(
                FacilityUpdateRequest(
                    // name 변경
                    "부분업데이트이름",
                    // code 유지
                    null,
                    // description 유지
                    null,
                    // thumbnail 유지
                    null,
                    // lon 유지
                    null,
                    // lat 유지
                    null,
                    // locationMeta 유지
                    null,
                ),
                // floors 변경 없음
                listOf(),
            )

        // when
        buildingService.update(id, request)

        // then
        val updated = facilityService.findById(id)
        assertThat(updated.name).isEqualTo("부분업데이트이름")
        assertThat(updated.description).isEqualTo(originalDescription)
    }

    @Test
    @DisplayName("필드를 null 로 업데이트하여도 유지 (Patch)")
    fun update_SetFieldToNull() {
        // given
        val id = buildingService.save(createRequest)

        val request =
            BuildingUpdateRequest(
                FacilityUpdateRequest(
                    // name 유지
                    null,
                    // code 유지
                    null,
                    // description 을 null 로 (이미 null 일 수 있으므로 이후 no-op 과 구분 위해 먼저 값 설정)
                    null,
                    null,
                    null,
                    null,
                    null,
                ),
                listOf(),
            )

        // when
        buildingService.update(id, request)

        // then
        val updated = facilityService.findById(id)
        assertThat(updated.description).isEqualTo(createRequest.facility.description)
    }

    @Test
    @DisplayName("No-Op 업데이트: 동일 데이터로 업데이트 시 변경 없음")
    fun update_NoOp_DoesNotChangeData() {
        // given
        val id = buildingService.save(createRequest)
        val before = facilityService.findById(id)

        val request =
            BuildingUpdateRequest(
                FacilityUpdateRequest(
                    before.name,
                    before.code,
                    before.description,
                    before.thumbnailFileId,
                    before.position?.lon,
                    before.position?.lat,
                    before.position?.locationMeta,
                ),
                listOf(),
            )

        // when
        buildingService.update(id, request)

        // then
        val after = facilityService.findById(id)
        assertThat(after.name).isEqualTo(before.name)
        assertThat(after.code).isEqualTo(before.code)
        assertThat(after.description).isEqualTo(before.description)
    }

    @Test
    @DisplayName("PUT 업데이트: 모든 필드를 교체")
    fun putUpdate_FullUpdate_ReplacesFields() {
        // given
        val id = buildingService.save(createRequest)

        val putRequest =
            BuildingUpdateRequest(
                FacilityUpdateRequest(
                    "풀업데이트이름",
                    "CODE2",
                    "풀업데이트설명",
                    // 썸네일 제거
                    null,
                    120.0,
                    30.0,
                    "{\"height\":10}",
                ),
                // floors 를 비워서 반영
                mutableListOf(),
            )

        // when
        buildingService.putUpdate(id, putRequest)

        // then
        val updated = facilityService.findById(id)
        assertThat(updated.name).isEqualTo("풀업데이트이름")
        assertThat(updated.code).isEqualTo("CODE2")
        assertThat(updated.description).isEqualTo("풀업데이트설명")
        assertThat(buildingService.findById(id).floors).isEmpty()
    }

    @Test
    @DisplayName("존재하지 않는 ID 삭제 시 예외 발생")
    fun delete_WithNonExistingId_ThrowsCustomException() {
        // given
        val nonExistingId = 987654321L

        // when & then
        assertThatThrownBy {
            buildingService.delete(nonExistingId)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("동일한 코드로 두 번째 건물 생성 시 중복 예외 발생")
    fun save_WithDuplicateCode_ThrowsCustomException() {
        // given
        buildingService.save(createRequest)

        val dupFacilityRequest =
            FacilityCreateRequest(
                // same code as in setUp
                "다른이름",
                "AAA",
                "설명",
                null,
                null,
                null,
                null,
                null,
            )
        val dupCreate = BuildingCreateRequest(dupFacilityRequest, mutableListOf())

        // when & then
        assertThatThrownBy {
            buildingService.save(dupCreate)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("다른 건물의 코드로 업데이트 시 중복 예외 발생")
    fun update_ToDuplicateCode_ThrowsCustomException() {
        // given
        // code AAA
        buildingService.save(createRequest)

        val facilityRequest2 =
            FacilityCreateRequest("두번째", "BBB", "desc", null, null, null, null, null)
        val secondId = buildingService.save(BuildingCreateRequest(facilityRequest2, mutableListOf()))

        val updateToDup =
            BuildingUpdateRequest(
                FacilityUpdateRequest(
                    // duplicate code
                    null,
                    "AAA",
                    null,
                    null,
                    null,
                    null,
                    null,
                ),
                listOf(),
            )

        // when & then
        assertThatThrownBy {
            buildingService.update(secondId, updateToDup)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("층 정보를 빈 리스트로 업데이트하면 모든 층이 제거된다")
    fun update_FloorsToEmpty_RemovesAllFloors() {
        // given
        // has one floor
        val id = buildingService.save(createRequest)
        val clearFloors =
            BuildingUpdateRequest(
                FacilityUpdateRequest(null, null, null, null, null, null, null),
                mutableListOf(),
            )

        // when
        buildingService.update(id, clearFloors)

        // then
        assertThat(buildingService.findById(id).floors).isEmpty()
    }
}
