package com.pluxity.station

import com.pluxity.config.MockBeansConfig
import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.facility.dto.FacilityUpdateRequest
import com.pluxity.facility.floor.FloorRepository
import com.pluxity.facility.floor.dto.FloorRequest
import com.pluxity.global.exception.CustomException
import com.pluxity.station.dto.StationCreateRequest
import com.pluxity.station.dto.StationResponse
import com.pluxity.station.dto.StationUpdateInfo
import com.pluxity.station.dto.StationUpdateRequest
import com.pluxity.util.TestFileUploader
import org.assertj.core.api.Assertions
import org.assertj.core.api.iterable.ThrowingExtractor
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class StationServiceTest
    @Autowired
    constructor(
        private val stationService: StationService,
        private val stationRepository: StationRepository,
        private val floorRepository: FloorRepository,
        private val lineRepository: LineRepository,
        private val stationLineRepository: StationLineRepository,
        private val stationCodeRepository: StationCodeRepository,
        private val testFileUploader: TestFileUploader,
    ) {
        // --- Create(save) Test ---
        @Test
        @DisplayName("성공: 모든 필드(노선, 역코드 포함)를 포함한 요청으로 역을 생성하고, 모든 응답 필드와 DB 상태를 상세히 검증한다")
        fun save_WithValidRequest_SavesStationAndAllRelations() {
            // GIVEN: 역 생성에 필요한 모든 데이터 준비 (파일, 층, 노선, 역코드)
            val drawingFileId = testFileUploader.initiateTestFileUpload("station.dwg")
            val thumbnailFileId = testFileUploader.initiateTestFileUpload("thumbnail.png")
            val line2Id = createAndSaveLine("2호선", "LINE_2").requiredId
            val lineSinbundangId = createAndSaveLine("신분당선", "LINE_SIN").requiredId

            val request =
                StationCreateRequest(
                    FacilityCreateRequest(
                        name = "강남역",
                        code = "GANGNAM_ST",
                        description = "2호선, 신분당선 환승역",
                        drawingFileId = drawingFileId,
                        thumbnailFileId = thumbnailFileId,
                        lon = 127.0276,
                        lat = 37.4979,
                        locationMeta = "{\"congestion\":\"high\"}",
                    ),
                    listOf(FloorRequest("B1층", "-1"), FloorRequest("B2층", "-2")),
                    listOf(line2Id, lineSinbundangId),
                    mutableListOf("222", "D07"),
                )

            // WHEN: 역 생성
            val createdStationId = stationService.save(request)

            // THEN: 응답 DTO 검증
            val response = stationService.findById(createdStationId)
            Assertions.assertThat(response.facility.id).isEqualTo(createdStationId)
            Assertions.assertThat(response.facility.name).isEqualTo("강남역")
            Assertions.assertThat(response.facility.code).isEqualTo("GANGNAM_ST")
            Assertions.assertThat(response.facility.description).isEqualTo("2호선, 신분당선 환승역")
            Assertions.assertThat(response.facility.drawing.id).isEqualTo(drawingFileId)
            Assertions.assertThat(response.facility.thumbnail.originalFileName).isEqualTo("thumbnail.png")
            Assertions.assertThat(response.facility.lon).isEqualTo(127.0276)
            Assertions.assertThat(response.facility.locationMeta).isEqualTo("{\"congestion\":\"high\"}")
            Assertions
                .assertThat(response.floors)
                .hasSize(2)
                .extracting("name")
                .containsExactly("B1층", "B2층")
            Assertions
                .assertThat(response.stationInfo.lineIds)
                .containsExactlyInAnyOrder(line2Id, lineSinbundangId)
            Assertions.assertThat(response.stationInfo.stationCodes).containsExactlyInAnyOrder("222", "D07")

            // THEN: 데이터베이스 최종 상태 직접 검증
            val savedStation = stationRepository.findById(createdStationId).orElseThrow()
            Assertions.assertThat(savedStation.name).isEqualTo("강남역")
            Assertions.assertThat(floorRepository.findAllByFacility(savedStation)).hasSize(2)
            val stationLines =
                stationLineRepository
                    .findAll()
                    .filter { it.station.id == savedStation.id }

            Assertions.assertThat(stationLines).hasSize(2)
            val stationCodes =
                stationCodeRepository
                    .findAll()
                    .filter { it.station.id == savedStation.id }

            Assertions
                .assertThat(stationCodes)
                .hasSize(2)
                .extracting<String, RuntimeException>(StationCode::code)
                .containsExactlyInAnyOrder("222", "D07")
        }

        @Test
        @DisplayName("성공: 선택적 필드(노선, 역코드, 층)가 null이거나 비어있을 때 역 생성이 성공한다")
        fun save_WithNullOrEmptyOptionalFields_Succeeds() {
            // GIVEN: 필수 필드만 채운 요청
            val request =
                StationCreateRequest(
                    FacilityCreateRequest(name = "단일역", code = "SINGLE_ST"),
                    emptyList(),
                    emptyList(),
                    // 층, 노선, 역코드 정보 없음
                    emptyList(),
                )

            // WHEN: 역 생성
            val createdStationId = stationService.save(request)

            // THEN: 생성된 역 정보 검증
            val response = stationService.findById(createdStationId)
            Assertions.assertThat(response.facility.name).isEqualTo("단일역")
            Assertions.assertThat(response.floors).isNotNull().isEmpty()
            Assertions.assertThat(response.stationInfo.lineIds).isNotNull().isEmpty()
            Assertions.assertThat(response.stationInfo.stationCodes).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("실패: 중복된 코드로 역 생성 시 예외가 발생한다")
        fun save_WithDuplicateCode_ThrowsCustomException() {
            // GIVEN: 기준 역 생성
            stationService.save(
                StationCreateRequest(
                    FacilityCreateRequest(name = "기준역", code = "DUPE_CODE"),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                ),
            )

            val duplicateRequest =
                StationCreateRequest(
                    FacilityCreateRequest(name = "다른역", code = "DUPE_CODE"),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                )

            // WHEN & THEN: 동일한 코드로 생성 시도 시 예외 발생
            assertThrows<CustomException> { stationService.save(duplicateRequest) }
        }

        // --- Read Test ---
        @Test
        @DisplayName("성공: 전체 역 조회 시 상세 정보가 포함된 목록을 반환한다")
        fun findAll_ReturnsListOfDetailedResponses() {
            // GIVEN: 2개의 서로 다른 역 생성
            val line1Id = createAndSaveLine("1호선", "L1").requiredId
            stationService.save(
                StationCreateRequest(
                    FacilityCreateRequest(name = "역A", code = "STA_A"),
                    emptyList(),
                    listOf(line1Id),
                    mutableListOf("133"),
                ),
            )
            stationService.save(
                StationCreateRequest(
                    FacilityCreateRequest(name = "역B", code = "STA_B"),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                ),
            )

            // WHEN: 전체 역 조회
            val responses: List<StationResponse> = stationService.findAll()

            // THEN: 목록 및 포함된 내용 검증
            Assertions.assertThat(responses).hasSize(2)
            val stationA =
                responses
                    .firstOrNull { it.facility.code == "STA_A" }
                    ?: throw IllegalArgumentException()
            Assertions.assertThat(stationA.stationInfo.lineIds).hasSize(1).contains(line1Id)
            Assertions.assertThat(stationA.stationInfo.stationCodes).hasSize(1).contains("133")
        }

        @Test
        @DisplayName("성공: 역이 없는 경우 전체 조회 시 빈 리스트를 반환한다")
        fun findAll_WhenNoStationsExist_ReturnsEmptyList() {
            // GIVEN: 데이터 없음
            // WHEN
            val responses: List<StationResponse> = stationService.findAll()
            // THEN
            Assertions.assertThat(responses).isNotNull().isEmpty()
        }

        // --- Update(putUpdate) Test ---
        @Test
        @DisplayName("성공(PUT): 모든 필드를 교체하는 수정 요청 시, null/빈리스트로 보낸 필드는 DB에서 삭제/초기화된다")
        fun putUpdate_FullReplace_ReplacesAllFields() {
            // GIVEN: 원본 데이터 생성
            val lineAId = createAndSaveLine("A노선", "LA").requiredId
            val lineBId = createAndSaveLine("B노선", "LB").requiredId
            val stationId =
                stationService.save(
                    StationCreateRequest(
                        FacilityCreateRequest(name = "원본역", code = "ORI_ST", description = "설명"),
                        listOf(FloorRequest("1층", "1")),
                        listOf(lineAId),
                        mutableListOf("A01"),
                    ),
                )
            val lineCId = createAndSaveLine("C노선", "LC").requiredId

            // GIVEN: 원본과 완전히 다른 교체 요청 (층, 노선, 역코드 모두 변경, 설명은 null로)
            val putRequest =
                StationUpdateRequest(
                    FacilityUpdateRequest(name = "교체된역", code = "PUT_ST"),
                    // 층 정보 삭제
                    emptyList(),
                    StationUpdateInfo(listOf(lineBId, lineCId), mutableListOf("B01", "C01")),
                )

            // WHEN: PUT 업데이트 실행
            stationService.putUpdate(stationId, putRequest)

            // THEN: 응답 DTO 검증 (StationResponse 구조에 맞게 수정)
            val response = stationService.findById(stationId)
            Assertions.assertThat(response.facility.name).isEqualTo("교체된역")
            Assertions.assertThat(response.facility.code).isEqualTo("PUT_ST")
            Assertions.assertThat(response.facility.description).isNull() // null로 교체됨
            Assertions.assertThat(response.floors).isEmpty() // 빈 리스트로 교체됨
            Assertions.assertThat(response.stationInfo.lineIds).containsExactlyInAnyOrder(lineBId, lineCId)
            Assertions.assertThat(response.stationInfo.stationCodes).containsExactlyInAnyOrder("B01", "C01")

            // THEN: DB 직접 검증 (findAll + filter 방식으로 수정)
            Assertions
                .assertThat(
                    floorRepository
                        .findAll()
                        .filter { it.facility?.id == stationId },
                ).isEmpty()

            val stationLines =
                stationLineRepository
                    .findAll()
                    .filter { it.station.id == stationId }

            Assertions
                .assertThat(stationLines)
                .hasSize(2)
                .extracting<Long, RuntimeException>(ThrowingExtractor { it.line.id })
                .containsExactlyInAnyOrder(lineBId, lineCId)
        }

        // --- Delete Test ---
        @Test
        @DisplayName("성공: 역을 삭제하면 해당 역과 하위 층, 노선/역코드 연결 정보가 모두 DB에서 삭제된다")
        fun delete_RemovesStationAndAllAssociatedRelations() {
            // GIVEN: 층, 노선, 코드가 있는 역 생성
            val lineId = createAndSaveLine("삭제될노선", "DEL_L").requiredId
            val stationId =
                stationService.save(
                    StationCreateRequest(
                        FacilityCreateRequest(name = "삭제될역", code = "DEL_ST"),
                        listOf(FloorRequest("1층", "1")),
                        listOf(lineId),
                        mutableListOf("DEL_C"),
                    ),
                )

            // GIVEN: DB에 관계 데이터가 있는지 확인 (findAll + filter 방식으로 수정)
            Assertions.assertThat(stationRepository.findById(stationId)).isPresent()
            Assertions
                .assertThat(
                    floorRepository
                        .findAll()
                        .any { it.facility?.id == stationId },
                ).isTrue()
            Assertions
                .assertThat(
                    stationLineRepository
                        .findAll()
                        .any { it.station.id == stationId },
                ).isTrue()
            Assertions
                .assertThat(
                    stationCodeRepository
                        .findAll()
                        .any { it.station.id == stationId },
                ).isTrue()

            // WHEN: 역 삭제
            stationService.delete(stationId)

            // THEN: 역과 모든 관계 데이터가 삭제되었는지 확인 (findAll + filter 방식으로 수정)
            Assertions.assertThat(stationRepository.findById(stationId)).isEmpty()
            Assertions
                .assertThat(
                    floorRepository
                        .findAll()
                        .none { it.facility?.id == stationId },
                ).isTrue()
            Assertions
                .assertThat(
                    stationLineRepository
                        .findAll()
                        .none { it.station.id == stationId },
                ).isTrue()
            Assertions
                .assertThat(
                    stationCodeRepository
                        .findAll()
                        .none { it.station.id == stationId },
                ).isTrue()

            assertThrows<CustomException> { stationService.findById(stationId) }
        }

        // --- Relation Management Test (addLineToStation / removeLineFromStation) ---
        @Test
        @DisplayName("성공: 역에 노선을 추가하고, 중복 추가는 무시된다")
        fun addLineToStation_AddsRelationAndIgnoresDuplicate() {
            // GIVEN
            val stationId = createAndSaveSimpleStation("테스트역", "T_ST")
            val lineId = createAndSaveLine("테스트노선", "T_L").requiredId

            // WHEN: 첫 번째 추가
            stationService.addLineToStation(stationId, lineId)

            // THEN: 관계가 생성되었는지 확인 (findAll + filter 방식으로 수정)
            Assertions
                .assertThat(
                    stationLineRepository
                        .findAll()
                        .filter { it.station.id == stationId },
                ).hasSize(1)

            // WHEN: 두 번째 (중복) 추가
            stationService.addLineToStation(stationId, lineId)

            // THEN: 관계 개수가 변하지 않았는지 확인 (중복 무시)
            Assertions
                .assertThat(
                    stationLineRepository
                        .findAll()
                        .filter { it.station.id == stationId },
                ).hasSize(1)
        }

        @Test
        @DisplayName("성공: 역에서 노선을 제거한다")
        fun removeLineFromStation_RemovesRelation() {
            // GIVEN: 노선이 연결된 역 생성
            val lineId = createAndSaveLine("제거될노선", "REM_L").requiredId
            val stationId =
                stationService.save(
                    StationCreateRequest(
                        FacilityCreateRequest(name = "역", code = "ST"),
                        emptyList(),
                        listOf(lineId),
                        emptyList(),
                    ),
                )
            Assertions
                .assertThat(
                    stationLineRepository
                        .findAll()
                        .any { it.station.id == stationId },
                ).isTrue()

            // WHEN: 노선 제거
            stationService.removeLineFromStation(stationId, lineId)

            // THEN: 관계가 삭제되었는지 확인
            Assertions
                .assertThat(
                    stationLineRepository
                        .findAll()
                        .none { it.station.id == stationId },
                ).isTrue()
        }

        // --- Helper Methods ---
        private fun createAndSaveLine(
            name: String,
            color: String,
        ): Line = lineRepository.save(Line(name = name, color = color))

        private fun createAndSaveSimpleStation(
            name: String,
            code: String,
        ): Long =
            stationService.save(
                StationCreateRequest(
                    FacilityCreateRequest(name = name, code = code),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                ),
            )
    }
