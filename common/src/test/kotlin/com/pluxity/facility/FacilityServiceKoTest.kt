package com.pluxity.facility

import base.entity.withAudit
import base.entity.withId
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
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.entity.FileEntity
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import facility.dummyCreateFacilityRequest
import facility.dummyUpdateFacilityRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.springframework.data.repository.findByIdOrNull

class FacilityServiceKoTest :
    BehaviorSpec({
        val facilityRepository = mockk<FacilityRepository>()
        val fileService = mockk<FileService>(relaxed = true)
        val facilityHistoryService = mockk<FacilityHistoryService>()
        val facilityPathService = mockk<FacilityPathService>()
        val floorService = mockk<FloorService>()

        val facilityService =
            FacilityService(
                facilityRepository,
                fileService,
                facilityHistoryService,
                facilityPathService,
                floorService,
            )

        Given("시설 생성 요청을 할 때") {
            When("코드가 중복이라면") {
                val request = dummyCreateFacilityRequest()
                val facility = FacilityInstance(request.name)

                every { facilityRepository.existsByCode(request.code) } returns true

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.save(facility, request)
                    }

                Then("DUPLICATE_FACILITY_CODE 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.DUPLICATE_FACILITY_CODE
                    verify(exactly = 1) { facilityRepository.existsByCode(request.code) }
                    verify(exactly = 0) { facilityRepository.save(any()) }
                }
            }

            When("정상적으로 시설 생성이 성공하면") {
                val generatedId = 100L
                val drawingFileId = 10L
                val thumbnailFileId = 20L

                val request = FacilityCreateRequest("시설", "CODE", "설명", drawingFileId, thumbnailFileId, null, null, null)
                val facility = FacilityInstance(request.name)
                val savedFacility = FacilityInstance(request.name, request.code).withId(generatedId)

                val drawingFile = mockk<FileEntity> { every { id } returns request.drawingFileId!! }
                val thumbnailFile = mockk<FileEntity> { every { id } returns request.thumbnailFileId!! }

                every { facilityRepository.existsByCode(request.code) } returns false
                every { facilityRepository.save(any<Facility>()) } returns savedFacility

                every { fileService.finalizeUpload(any(), any()) } returnsMany listOf(drawingFile, thumbnailFile)
                every { facilityHistoryService.save(any(), any(), any()) } just runs

                val result = facilityService.save(facility, request)

                Then("저장된 시설을 반환하며 ID와 파일 경로가 올바르게 전파된다.") {
                    result shouldBe savedFacility

                    val expectedPath = "facilities/$generatedId/"

                    verify(exactly = 1) {
                        fileService.finalizeUpload(request.drawingFileId!!, match { it == expectedPath })
                    }
                    verify(exactly = 1) {
                        fileService.finalizeUpload(request.thumbnailFileId!!, match { it == expectedPath })
                    }

                    verify(exactly = 1) {
                        facilityHistoryService.save(request.drawingFileId!!, generatedId, "최초등록")
                    }
                }
            }

            When("도면 파일 ID가 있으면") {
                val drawingFileId = 10L
                val request = FacilityCreateRequest("시설", "CODE", "설명", drawingFileId, null, null, null, null)
                val facility = FacilityInstance(request.name)
                val savedFacility = FacilityInstance(request.name, request.code).withId(1L)
                val fileEntity = mockk<FileEntity> { every { id } returns drawingFileId }

                every { facilityRepository.existsByCode(request.code) } returns false
                every { facilityRepository.save(any<Facility>()) } returns savedFacility
                every { fileService.finalizeUpload(drawingFileId, any<String>()) } returns fileEntity
                every { facilityHistoryService.save(drawingFileId, 1L, "최초등록") } just runs

                facilityService.save(facility, request)

                Then("히스토리가 저장된다.") {
                    verify(exactly = 1) { facilityHistoryService.save(drawingFileId, 1L, "최초등록") }
                }
            }

            When("도면 파일 ID가 없으면") {
                val request = FacilityCreateRequest("시설", "CODE", "설명", null, null, null, null, null)
                val facility = FacilityInstance(request.name)
                val savedFacility = FacilityInstance(request.name, request.code).withId(1L)

                every { facilityRepository.existsByCode(request.code) } returns false
                every { facilityRepository.save(any<Facility>()) } returns savedFacility

                facilityService.save(facility, request)

                Then("히스토리가 저장되지 않는다.") {
                    verify(exactly = 0) { facilityHistoryService.save(any<Long>(), any<Long>(), any<String>()) }
                }
            }

            When("썸네일 파일 ID만 있으면") {
                val thumbnailFileId = 10L
                val request = FacilityCreateRequest("시설", "CODE", "설명", null, thumbnailFileId, null, null, null)
                val facility = FacilityInstance(request.name)
                val savedFacility = FacilityInstance(request.name, request.code).withId(1L)
                val fileEntity = mockk<FileEntity> { every { id } returns thumbnailFileId }

                every { facilityRepository.existsByCode(request.code) } returns false
                every { facilityRepository.save(any<Facility>()) } returns savedFacility
                every { fileService.finalizeUpload(thumbnailFileId, any<String>()) } returns fileEntity

                facilityService.save(facility, request)

                Then("히스토리가 저장되지 않는다.") {
                    verify(exactly = 0) { facilityHistoryService.save(thumbnailFileId, 1L, "최초등록") }
                }
            }

            When("위치 정보(Position)가 포함되어 있으면") {
                val request = FacilityCreateRequest("시설", "CODE", "설명", null, null, 127.0, 37.0, "정문 앞")
                val facility = FacilityInstance(request.name)
                val savedFacility = FacilityInstance(request.name, request.code).withId(1L)

                every { facilityRepository.existsByCode(request.code) } returns false
                every { facilityRepository.save(any<Facility>()) } returns savedFacility

                // When
                facilityService.save(facility, request)

                Then("엔티티의 위치 정보가 업데이트된다.") {
                    facility.position?.lon shouldBe 127.0
                    facility.position?.lat shouldBe 37.0
                    facility.position?.locationMeta shouldBe "정문 앞"
                }
            }
        }

        Given("코드로 시설 조회 요청을 할 때") {
            When("코드가 유효하다면") {
                val code = "VALID_CODE"
                val facility = FacilityInstance("시설", code)

                every { facilityRepository.findByCode(code) } returns facility

                val result = facilityService.findByCode(code)

                Then("시설을 반환한다.") {
                    result shouldBe facility
                    verify(exactly = 1) { facilityRepository.findByCode(code) }
                }
            }

            When("코드가 유효하지 않다면") {
                val invalidCode = "INVALID_CODE"

                every { facilityRepository.findByCode(invalidCode) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.findByCode(invalidCode)
                    }

                Then("NOT_FOUND_FACILITY_CODE 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY_CODE
                    verify(exactly = 1) { facilityRepository.findByCode(invalidCode) }
                }
            }
        }

        Given("ID로 시설 조회 요청을 할 때") {
            When("ID가 유효하다면") {
                val id = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id)

                every { facilityRepository.findByIdOrNull(id) } returns facility

                val result = facilityService.findById(id)

                Then("시설을 반환한다.") {
                    result shouldBe facility
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(id) }
                }
            }

            When("ID가 유효하지 않다면") {
                val invalidId = 999L

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.findById(invalidId)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    exception.message shouldBe ErrorCode.NOT_FOUND_FACILITY.getMessage().format(invalidId)
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                }
            }
        }

        Given("시설 수정 요청을 할 때") {
            When("수정 요청한 코드가 중복이라면") {
                val id = 1L
                val facility = FacilityInstance("시설", "OLD_CODE").withId(id)
                val newCode = "NEW_CODE"
                val request = FacilityUpdateRequest("시설", newCode, null, null, null, null, null)

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { facilityRepository.existsByCode(newCode) } returns true

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.putUpdate(id, request)
                    }

                Then("DUPLICATE_FACILITY_CODE 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.DUPLICATE_FACILITY_CODE
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(id) }
                    verify(exactly = 1) { facilityRepository.existsByCode(newCode) }
                }
            }

            When("정상적으로 시설 수정이 성공하면") {
                val id = 1L
                val request = dummyUpdateFacilityRequest()
                val facility = FacilityInstance("시설", request.code).withId(id)

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { facilityRepository.existsByCode(request.code!!) } returns false

                facilityService.putUpdate(id, request)

                Then("시설 정보가 업데이트된다.") {
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(id) }
                }
            }

            When("썸네일 파일 ID가 변경되면") {
                val id = 1L
                val oldThumbnailId = 10L
                val newThumbnailId = 20L
                val request = FacilityUpdateRequest("시설", null, null, newThumbnailId, null, null, null)
                val facility = FacilityInstance("시설", null, null, null, null, oldThumbnailId).withId(id)
                val fileEntity = mockk<FileEntity>()
                every { fileEntity.id } returns newThumbnailId

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { fileService.finalizeUpload(newThumbnailId, any()) } returns fileEntity

                facilityService.putUpdate(id, request)

                Then("새로운 파일이 finalize된다.") {
                    verify(exactly = 1) { fileService.finalizeUpload(newThumbnailId, any()) }
                    facility.thumbnailFileId shouldBe newThumbnailId
                }
            }

            When("썸네일 파일 ID가 동일하면") {
                val id = 1L
                val thumbnailId = 10L
                val request = FacilityUpdateRequest("시설", null, null, thumbnailId, null, null, null)
                val facility = FacilityInstance("시설", null, null, null, null, thumbnailId).withId(id)

                every { facilityRepository.findByIdOrNull(id) } returns facility

                facilityService.putUpdate(id, request)

                Then("파일이 finalize되지 않는다.") {
                    verify(exactly = 0) { fileService.finalizeUpload(any(), any()) }
                }
            }
        }

        Given("시설 전체 수정 요청을 할 때") {
            When("정상적으로 시설 전체 수정이 성공하면") {
                val id = 1L
                val request = dummyUpdateFacilityRequest()
                val facility = FacilityInstance("시설", "OLD_CODE").withId(id)

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { facilityRepository.existsByCode(request.code!!) } returns false

                facilityService.putUpdate(id, request)

                Then("시설 정보가 전체 업데이트된다.") {
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(id) }
                }
            }

            When("썸네일 파일 ID를 null로 변경하면") {
                val id = 1L
                val request = FacilityUpdateRequest("시설", null, null, null, null, null, null)
                val facility = FacilityInstance("시설", null, null, null, null, 10L).withId(id)

                every { facilityRepository.findByIdOrNull(id) } returns facility

                facilityService.putUpdate(id, request)

                Then("썸네일 파일 ID가 null로 설정된다.") {
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(id) }
                }
            }
        }

        Given("시설 삭제 요청을 할 때") {
            When("ID가 유효하지 않다면") {
                val invalidId = 999L

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.deleteFacility(invalidId)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                    verify(exactly = 0) { facilityRepository.delete(any()) }
                }
            }

            When("정상적으로 시설 삭제가 성공하면") {
                val id = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id)

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { facilityRepository.delete(facility) } just runs

                facilityService.deleteFacility(id)

                Then("시설이 삭제된다.") {
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(id) }
                    verify(exactly = 1) { facilityRepository.delete(facility) }
                }
            }
        }

        Given("시설 히스토리 조회 요청을 할 때") {
            When("시설 ID가 유효하지 않다면") {
                val invalidId = 999L

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.findFacilityHistories(invalidId)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                    verify(exactly = 0) { facilityHistoryService.findByFacilityId(any()) }
                }
            }

            When("정상적으로 히스토리 조회가 성공하면") {
                val id = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id)
                val histories = emptyList<com.pluxity.facility.dto.FacilityHistoryResponse>()

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { facilityHistoryService.findByFacilityId(id) } returns histories

                val result = facilityService.findFacilityHistories(id)

                Then("히스토리 리스트를 반환한다.") {
                    result shouldBe histories
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(id) }
                    verify(exactly = 1) { facilityHistoryService.findByFacilityId(id) }
                }
            }
        }

        Given("도면 파일 수정 요청을 할 때") {
            When("시설 ID가 유효하지 않다면") {
                val invalidId = 999L
                val request = FacilityDrawingUpdateRequest(10L, "주석")

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.updateDrawingFile(invalidId, request)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                    verify(exactly = 0) { fileService.finalizeUpload(any(), any()) }
                }
            }

            When("정상적으로 도면 파일 수정이 성공하면") {
                val id = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id).withAudit()
                val request = FacilityDrawingUpdateRequest(10L, "주석")
                val fileEntity = mockk<FileEntity>()

                every { fileEntity.id } returns request.drawingFileId
                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { fileService.finalizeUpload(request.drawingFileId, any()) } returns fileEntity
                every { facilityHistoryService.save(request.drawingFileId, id, request.comment ?: "") } just runs

                facilityService.updateDrawingFile(id, request)

                Then("도면 파일이 업데이트되고 히스토리가 저장된다.") {
                    verify(exactly = 1) { fileService.finalizeUpload(request.drawingFileId, any()) }
                    verify(exactly = 1) { facilityHistoryService.save(request.drawingFileId, id, request.comment ?: "") }
                }
            }
        }

        Given("시설 경로 저장 요청을 할 때") {
            When("시설 ID가 유효하지 않다면") {
                val invalidId = 999L
                val request = FacilityPathSaveRequest("경로명", "SUBWAY", "{}")

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.savePath(invalidId, request)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                    verify(exactly = 0) { facilityPathService.save(any(), any(), any(), any()) }
                }
            }

            When("정상적으로 경로 저장이 성공하면") {
                val id = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id)
                val request = FacilityPathSaveRequest("경로명", "SUBWAY", "{}")

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { facilityPathService.save(facility, request.name, request.type, request.path) } just runs

                facilityService.savePath(id, request)

                Then("경로가 저장된다.") {
                    verify(exactly = 1) { facilityPathService.save(facility, request.name, request.type, request.path) }
                }
            }
        }

        Given("시설 경로 수정 요청을 할 때") {
            When("시설 ID가 유효하지 않다면") {
                val invalidId = 999L
                val pathId = 1L
                val request = FacilityPathUpdateRequest("수정경로명", "WAY", "{}")

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.updatePath(invalidId, pathId, request)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                    verify(exactly = 0) { facilityPathService.update(any(), any(), any(), any()) }
                }
            }

            When("정상적으로 경로 수정이 성공하면") {
                val id = 1L
                val pathId = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id)
                val request = FacilityPathUpdateRequest("수정경로명", "WAY", "{}")

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { facilityPathService.update(pathId, request.name, request.type, request.path) } just runs

                facilityService.updatePath(id, pathId, request)

                Then("경로가 수정된다.") {
                    verify(exactly = 1) { facilityPathService.update(pathId, request.name, request.type, request.path) }
                }
            }
        }

        Given("시설 경로 삭제 요청을 할 때") {
            When("시설 ID가 유효하지 않다면") {
                val invalidId = 999L
                val pathId = 1L

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.deletePath(invalidId, pathId)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                    verify(exactly = 0) { facilityPathService.delete(any()) }
                }
            }

            When("정상적으로 경로 삭제가 성공하면") {
                val id = 1L
                val pathId = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id)

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { facilityPathService.delete(pathId) } just runs

                facilityService.deletePath(id, pathId)

                Then("경로가 삭제된다.") {
                    verify(exactly = 1) { facilityPathService.delete(pathId) }
                }
            }
        }

        Given("시설 위치 수정 요청을 할 때") {
            When("시설 ID가 유효하지 않다면") {
                val invalidId = 999L
                val request = FacilityLocationUpdateRequest(127.0, 37.0, "{}")

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.updateLocation(invalidId, request)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                }
            }

            When("정상적으로 위치 수정이 성공하면") {
                val id = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id)
                val request = FacilityLocationUpdateRequest(127.0, 37.0, "{}")

                every { facilityRepository.findByIdOrNull(id) } returns facility

                facilityService.updateLocation(id, request)

                Then("위치가 업데이트된다.") {
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(id) }
                }
            }
        }

        Given("시설 층 수정 요청을 할 때") {
            When("시설 ID가 유효하지 않다면") {
                val invalidId = 999L
                val request = FacilityFloorUpdateRequest(emptyList())

                every { facilityRepository.findByIdOrNull(invalidId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.updateFloor(invalidId, request)
                    }

                Then("NOT_FOUND_FACILITY 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                    verify(exactly = 1) { facilityRepository.findByIdOrNull(invalidId) }
                    verify(exactly = 0) { floorService.update(any(), any()) }
                }
            }

            When("정상적으로 층 수정이 성공하면") {
                val id = 1L
                val facility = FacilityInstance("시설", "CODE").withId(id)
                val request = FacilityFloorUpdateRequest(emptyList())

                every { facilityRepository.findByIdOrNull(id) } returns facility
                every { floorService.update(facility, request.floors) } just runs

                facilityService.updateFloor(id, request)

                Then("층이 업데이트된다.") {
                    verify(exactly = 1) { floorService.update(facility, request.floors) }
                }
            }
        }

        Given("시설 목록 조회 요청을 할 때") {
            When("정상적으로 조회가 성공하면") {
                val facility1 = FacilityInstance("시설1", "CODE1", null, null, 10L, 20L).withId(1L).withAudit()
                val facility2 = FacilityInstance("시설2", "CODE2").withId(2L).withAudit()
                val facilities = listOf(facility1, facility2)

                every { facilityRepository.findAllByOrderByCreatedAtDesc() } returns facilities
                every { fileService.getFiles(any<List<Long>>()) } returns emptyList<FileResponse>()

                val result = facilityService.findAllFacilities()

                Then("시설 목록을 반환한다.") {
                    result.size shouldBe 2
                    verify(exactly = 1) { facilityRepository.findAllByOrderByCreatedAtDesc() }
                    verify(exactly = 1) { fileService.getFiles(any<List<Long>>()) }
                }
            }

            When("데이터가 없으면") {
                every { facilityRepository.findAllByOrderByCreatedAtDesc() } returns emptyList()

                val result = facilityService.findAllFacilities()

                Then("빈 리스트를 반환한다.") {
                    result shouldBe emptyList()
                    verify(exactly = 1) { facilityRepository.findAllByOrderByCreatedAtDesc() }
                }
            }
        }
    }) {
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
}
