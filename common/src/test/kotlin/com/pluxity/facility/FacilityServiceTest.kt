package com.pluxity.facility

import com.pluxity.config.MockBeansConfig
import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.facility.dto.FacilityDrawingUpdateRequest
import com.pluxity.facility.dto.FacilityFloorUpdateRequest
import com.pluxity.facility.dto.FacilityLocationUpdateRequest
import com.pluxity.facility.dto.FacilityPathSaveRequest
import com.pluxity.facility.dto.FacilityPathUpdateRequest
import com.pluxity.facility.dto.FacilityUpdateRequest
import com.pluxity.facility.history.FacilityHistoryService
import com.pluxity.facility.path.FacilityPathService
import com.pluxity.facility.strategy.FloorService
import com.pluxity.global.exception.CustomException
import com.pluxity.util.TestFileUploader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
class FacilityServiceTest
    @Autowired
    constructor(
        private val facilityService: FacilityService,
        private val facilityRepository: FacilityRepository,
        private val testFileUploader: TestFileUploader,
    ) {
        @MockitoBean
        private lateinit var facilityHistoryService: FacilityHistoryService

        @MockitoBean
        private lateinit var facilityPathService: FacilityPathService

        @MockitoBean
        private lateinit var floorService: FloorService

        @Entity
        @DiscriminatorValue("TEST")
        class FacilityInstance(
            name: String,
            code: String? = null,
            description: String? = null,
            historyComment: String? = null,
            drawingFileId: Long? = null,
            thumbnailFileId: Long? = null,
            position: FacilityPosition? = null,
            category: com.pluxity.facility.category.FacilityCategory? = null,
        ) : Facility(
                name = name,
                code = code,
                description = description,
                historyComment = historyComment,
                drawingFileId = drawingFileId,
                thumbnailFileId = thumbnailFileId,
                position = position,
            ) {
            init {
                category?.let { assignCategory(it) }
            }
        }

        @Test
        @DisplayName("성공: 유효한 요청으로 시설 생성 시 모든 필드가 정상적으로 저장된다")
        fun `save with valid request saves facility`() {
            // GIVEN
            val drawingFileId = testFileUploader.initiateTestFileUpload("drawing.dwg")
            val thumbnailFileId = testFileUploader.initiateTestFileUpload("thumb.png")
            val request =
                FacilityCreateRequest(
                    name = "서울역",
                    code = "SEOUL_ST",
                    description = "대한민국 수도의 관문",
                    drawingFileId = drawingFileId,
                    thumbnailFileId = thumbnailFileId,
                    lon = 126.97,
                    lat = 37.55,
                    locationMeta = "{'floor': 5}",
                )
            val facility =
                FacilityInstance(
                    name = request.name,
                    code = request.code,
                    description = request.description,
                    drawingFileId = request.drawingFileId,
                    thumbnailFileId = request.thumbnailFileId,
                )

            // WHEN
            val savedFacility = facilityService.save(facility, request)

            // THEN
            savedFacility.id.shouldNotBeNull()
            savedFacility.name shouldBe "서울역"
            savedFacility.code shouldBe "SEOUL_ST"
            savedFacility.description shouldBe "대한민국 수도의 관문"
            savedFacility.drawingFileId shouldBe drawingFileId
            savedFacility.thumbnailFileId shouldBe thumbnailFileId
            savedFacility.position?.lon shouldBe 126.97
            savedFacility.position?.lat shouldBe 37.55
            savedFacility.position?.locationMeta shouldBe "{'floor': 5}"

            verify(facilityHistoryService, times(1)).save(drawingFileId, savedFacility.requiredId, "최초등록")
        }

        @Test
        @DisplayName("도면 파일 없이 시설 생성 요청 시 히스토리가 등록되지 않는다.")
        fun `save without drawingFile request save facility`() {
            val drawingFileId = null
            val thumbnailFileId = testFileUploader.initiateTestFileUpload("thumb.png")
            val request =
                FacilityCreateRequest(
                    name = "서울역",
                    code = "SEOUL_ST",
                    description = "대한민국 수도의 관문",
                    drawingFileId = drawingFileId,
                    thumbnailFileId = thumbnailFileId,
                    lon = 126.97,
                    lat = 37.55,
                    locationMeta = "{'floor': 5}",
                )
            val facility =
                FacilityInstance(
                    name = request.name,
                    code = request.code,
                    description = request.description,
                    drawingFileId = request.drawingFileId,
                    thumbnailFileId = request.thumbnailFileId,
                )

            // when
            val savedFacility = facilityService.save(facility, request)

            // then
            verify(facilityHistoryService, never()).save(
                fileId = any(),
                facilityId = any(),
                comment = any(),
            )

            savedFacility.shouldNotBeNull()
            savedFacility.name shouldBe "서울역"
        }

        @Test
        @DisplayName("실패: 중복된 코드로 시설 생성 시 예외가 발생한다")
        fun `save with duplicate code throws CustomException`() {
            // GIVEN
            facilityService.save(
                FacilityInstance(name = "시설1", code = "DUP_CODE"),
                FacilityCreateRequest("시설1", "DUP_CODE", null, null, null, null, null, null),
            )

            val duplicateRequest = FacilityCreateRequest("시설2", "DUP_CODE", null, null, null, null, null, null)
            val facility2 = FacilityInstance(name = "시설2", code = "DUP_CODE")

            // WHEN & THEN
            shouldThrow<CustomException> {
                facilityService.save(facility2, duplicateRequest)
            }
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 조회 시 예외가 발생한다")
        fun `findById with non-existing id throws CustomException`() {
            shouldThrow<CustomException> {
                facilityService.findById(9999L)
            }
        }

        @Test
        @DisplayName("성공: update 요청 시 일부 필드만 정상적으로 수정된다")
        fun `update with partial request updates only provided fields`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("원본 이름", "ORI_CODE", "원본 설명", null, null),
                    FacilityCreateRequest("원본 이름", "ORI_CODE", "원본 설명", null, null, 1.0, 1.0, null),
                )

            val request = FacilityUpdateRequest("수정된 이름", null, null, null, 2.0, null, null)

            // WHEN
            facilityService.update(saved.requiredId, request)

            // THEN
            val updated = facilityService.findById(saved.requiredId)
            updated.name shouldBe "수정된 이름"
            updated.position?.lon shouldBe 2.0
            updated.code shouldBe "ORI_CODE"
            updated.description shouldBe "원본 설명"
        }

        @Test
        @DisplayName("성공: putUpdate 요청 시 모든 필드가 요청대로 덮어쓰기된다 (null 포함)")
        fun `putUpdate with full request overwrites all fields`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("원본 이름", "ORI_CODE", "원본 설명", null, null),
                    FacilityCreateRequest("원본 이름", "ORI_CODE", "원본 설명", null, null, 1.0, 1.0, null),
                )

            val request = FacilityUpdateRequest("수정된 이름", "UPD_CODE", null, null, 2.0, 2.0, "{}")

            // WHEN
            facilityService.putUpdate(saved.requiredId, request)

            // THEN
            val updated = facilityService.findById(saved.requiredId)
            updated.name shouldBe "수정된 이름"
            updated.code shouldBe "UPD_CODE"
            updated.description shouldBe null
            updated.position?.lat shouldBe 2.0
        }

        @Test
        @DisplayName("성공: update 요청 시 code가 같을때는 중복 체크를 하지 않는다.")
        fun `update with same code does not check for duplicate`() {
            // Given
            val saved =
                facilityService.save(
                    FacilityInstance("원본 이름", "ORI_CODE", "원본 설명", null, null),
                    FacilityCreateRequest("원본 이름", "ORI_CODE", "원본 설명", null, null, 1.0, 1.0, null),
                )

            val request = FacilityUpdateRequest("수정된 이름", "ORI_CODE", null, null, 2.0, null, null)

            facilityService.update(saved.requiredId, request)

            val updated = facilityService.findById(saved.requiredId)
            updated.code shouldBe "ORI_CODE"
            updated.name shouldBe "수정된 이름"
        }

        @Test
        @DisplayName("성공: update 요청 시 thumbnailFileId가 같을 때는 업로드 하지 않는다.")
        fun `update with same thumbnailFileId does not upload thumbnail`() {
            // Given
            val thumbnailFileId = testFileUploader.initiateTestFileUpload("thumb.png")
            val savedFacility =
                facilityService.save(
                    FacilityInstance("시설", "CODE", null, null),
                    FacilityCreateRequest("시설", "CODE", null, null, thumbnailFileId, null, null, null),
                )

            // When & Then
            val request = FacilityUpdateRequest("수정된 이름", null, null, thumbnailFileId, null, null, null)
            facilityService.update(savedFacility.requiredId, request)

            val updated = facilityService.findById(savedFacility.requiredId)
            updated.thumbnailFileId shouldBe thumbnailFileId
            updated.name shouldBe "수정된 이름"
        }

        @Test
        @DisplayName("성공: 시설 삭제 시 DB에서 소프트 삭제된다")
        fun `deleteFacility with existing id soft deletes facility`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("삭제될 시설", "DEL_CODE", null, null, null),
                    FacilityCreateRequest("삭제될 시설", "DEL_CODE", null, null, null, null, null, null),
                )

            // WHEN
            facilityService.deleteFacility(saved.requiredId)

            // THEN
            shouldThrow<CustomException> {
                facilityService.findById(saved.requiredId)
            }
        }

        @Test
        @DisplayName("성공: 도면 파일 업데이트 시 히스토리가 기록된다")
        fun `updateDrawingFile updates file and saves history`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("시설", "CODE", null, null, null),
                    FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null),
                )
            val newDrawingFileId = testFileUploader.initiateTestFileUpload("new_drawing.dwg")
            val request = FacilityDrawingUpdateRequest(newDrawingFileId, "도면 교체")

            // WHEN
            facilityService.updateDrawingFile(saved.requiredId, request)

            // THEN
            val updated = facilityService.findById(saved.requiredId)
            updated.drawingFileId shouldBe newDrawingFileId

            verify(facilityHistoryService, times(1)).save(newDrawingFileId, saved.requiredId, "도면 교체")
        }

        @Test
        @DisplayName("성공: 경로 저장 시 FacilityPathService가 호출된다")
        fun `savePath delegates to PathService`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("시설", "CODE", null, null, null),
                    FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null),
                )
            val request = FacilityPathSaveRequest("주 경로", "MAIN", "{}")

            // WHEN
            facilityService.savePath(saved.requiredId, request)

            // THEN
            verify(facilityPathService, times(1))
                .save(any<Facility>(), eq("주 경로"), eq("MAIN"), eq("{}"))
        }

        @Test
        @DisplayName("성공: 층 정보 업데이트 시 FloorService가 호출된다")
        fun `updateFloor delegates to FloorService`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("시설", "CODE", null, null, null),
                    FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null),
                )
            val request = FacilityFloorUpdateRequest(emptyList())

            // WHEN
            facilityService.updateFloor(saved.requiredId, request)

            // THEN
            verify(floorService, times(1)).update(any<Facility>(), any())
        }

        @Test
        @DisplayName("실패: 존재하지 않는 시설의 경로 저장 시 예외가 발생한다")
        fun `savePath on non-existing facility throws CustomException`() {
            // GIVEN
            val request = FacilityPathSaveRequest("주 경로", "MAIN", "{}")

            // WHEN & THEN
            shouldThrow<CustomException> {
                facilityService.savePath(9999L, request)
            }
        }

        @Test
        @DisplayName("성공: 일부 선택적 필드가 null일 때도 시설 생성이 성공한다")
        fun `save with null optional fields succeeds`() {
            // GIVEN
            val request = FacilityCreateRequest("필수 필드만 있는 시설", "MANDATORY", null, null, null, null, null, null)
            val facility = FacilityInstance(request.name, request.code, null, null, null)

            // WHEN
            val savedFacility = facilityService.save(facility, request)

            // THEN
            savedFacility.shouldNotBeNull()
            savedFacility.name shouldBe "필수 필드만 있는 시설"
            savedFacility.code shouldBe "MANDATORY"
            savedFacility.description shouldBe null
            savedFacility.drawingFileId shouldBe null
            savedFacility.position shouldBe null
        }

        @Test
        @DisplayName("실패: update 시 다른 시설이 사용 중인 코드로 변경하면 예외가 발생한다")
        fun `update with existing code of another facility throws CustomException`() {
            // GIVEN
            facilityService.save(
                FacilityInstance("시설1", "CODE1", null, null, null),
                FacilityCreateRequest("시설1", "CODE1", null, null, null, null, null, null),
            )
            val saved2 =
                facilityService.save(
                    FacilityInstance("시설2", "CODE2", null, null, null),
                    FacilityCreateRequest("시설2", "CODE2", null, null, null, null, null, null),
                )

            // WHEN & THEN
            val request = FacilityUpdateRequest("시설2", "CODE1", null, null, null, null, null)
            shouldThrow<CustomException> {
                facilityService.update(saved2.requiredId, request)
            }
        }

        @Test
        @DisplayName("성공: findAll 호출 시 모든 시설 목록을 반환한다")
        fun `findAll when facilities exist returns list of facilities`() {
            // GIVEN
            facilityService.save(
                FacilityInstance("시설1", "CODE1", null, null, null),
                FacilityCreateRequest("시설1", "CODE1", null, null, null, null, null, null),
            )
            facilityService.save(
                FacilityInstance("시설2", "CODE2", null, null, null),
                FacilityCreateRequest("시설2", "CODE2", null, null, null, null, null, null),
            )

            // WHEN
            val facilities = facilityService.findAll()

            // THEN
            facilities shouldHaveSize 2
        }

        @Test
        @DisplayName("성공: 시설이 없을 때 findAll 호출 시 빈 리스트를 반환한다")
        fun `findAll when no facilities exist returns empty list`() {
            // GIVEN
            facilityRepository.deleteAll()

            // WHEN
            val facilities = facilityService.findAll()

            // THEN
            facilities.shouldNotBeNull()
            facilities.shouldBeEmpty()
        }

        @Test
        @DisplayName("성공: 유효한 코드로 findByCode 호출 시 시설을 반환한다")
        fun `findByCode with valid code returns facility`() {
            // GIVEN
            val code = "VALID_CODE"
            facilityService.save(
                FacilityInstance("시설", code, null, null, null),
                FacilityCreateRequest("시설", code, null, null, null, null, null, null),
            )

            // WHEN
            val found = facilityService.findByCode(code)

            // THEN
            found.shouldNotBeNull()
            found.code shouldBe code
        }

        @Test
        @DisplayName("실패: 존재하지 않는 코드로 findByCode 호출 시 예외가 발생한다")
        fun `findByCode with non-existing code throws CustomException`() {
            shouldThrow<CustomException> {
                facilityService.findByCode("NON_EXISTING")
            }
        }

        @Test
        @DisplayName("성공: 경로 수정 시 FacilityPathService가 호출된다")
        fun `updatePath delegates to PathService`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("시설", "CODE", null, null, null),
                    FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null),
                )
            val request = FacilityPathUpdateRequest("수정된 경로", "SUB", "{}")

            // WHEN
            facilityService.updatePath(saved.requiredId, 1L, request)

            // THEN
            verify(facilityPathService, times(1)).update(1L, "수정된 경로", "SUB", "{}")
        }

        @Test
        @DisplayName("성공: 경로 삭제 시 FacilityPathService가 호출된다")
        fun `deletePath delegates to PathService`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("시설", "CODE", null, null, null),
                    FacilityCreateRequest("시설", "CODE", null, null, null, null, null, null),
                )

            // WHEN
            facilityService.deletePath(saved.requiredId, 1L)

            // THEN
            verify(facilityPathService, times(1)).delete(1L)
        }

        @Test
        @DisplayName("성공: 위치 정보 업데이트 시 좌표와 메타 정보가 변경된다")
        fun `updateLocation updates position correctly`() {
            // GIVEN
            val saved =
                facilityService.save(
                    FacilityInstance("시설", "CODE", null, null, null),
                    FacilityCreateRequest("시설", "CODE", null, null, null, 1.0, 1.0, null),
                )
            val request = FacilityLocationUpdateRequest(127.5, 37.5, "{'new_meta': true}")

            // WHEN
            facilityService.updateLocation(saved.requiredId, request)

            // THEN
            val updated = facilityService.findById(saved.requiredId)
            updated.position?.lon shouldBe 127.5
            updated.position?.lat shouldBe 37.5
            updated.position?.locationMeta shouldBe "{'new_meta': true}"
        }

        @Test
        @DisplayName("성공: findAllFacilities 호출 시 파일 정보가 포함된 시설 목록을 반환한다")
        fun `findAllFacilities when facilities exist returns list with file info`() {
            // GIVEN
            val drawingFileId = testFileUploader.initiateTestFileUpload("drawing1.dwg")
            val thumbnailFileId = testFileUploader.initiateTestFileUpload("thumb1.png")

            val facility1 =
                facilityService.save(
                    FacilityInstance("시설1", "CODE1", null, null, null),
                    FacilityCreateRequest("시설1", "CODE1", null, drawingFileId, thumbnailFileId, null, null, null),
                )
            val facility2 =
                facilityService.save(
                    FacilityInstance("시설2", "CODE2", null, null, null),
                    FacilityCreateRequest("시설2", "CODE2", null, null, null, null, null, null),
                )

            // WHEN
            val facilities = facilityService.findAllFacilities()
            println(facilities)

            // THEN
            val facility1Response = facilities.find { it.id == facility1.requiredId }!!
            val facility2Response = facilities.find { it.id == facility2.requiredId }!!

            facility1Response.name shouldBe "시설1"
            facility1Response.thumbnail.originalFileName shouldBe "thumb1.png"
            facility1Response.drawing.originalFileName shouldBe "drawing1.dwg"

            facility2Response.name shouldBe "시설2"
            facility2Response.thumbnail.originalFileName shouldBe null
            facility2Response.drawing.originalFileName shouldBe null
        }
    }
