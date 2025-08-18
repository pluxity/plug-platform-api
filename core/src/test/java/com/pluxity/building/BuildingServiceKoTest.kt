package com.pluxity.building

import com.pluxity.building.dto.dummyCreateBuildingRequest
import com.pluxity.building.dto.dummyUpdateBuildingRequest
import com.pluxity.building.entity.dummyBuilding
import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.facility.strategy.FloorService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import facility.floor.dummyFloorResponse
import file.dummyFileResponse
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.domain.Sort
import java.util.Optional

class BuildingServiceKoTest : BehaviorSpec({
    val fileService: FileService = mockk()
    val facilityService: FacilityService = mockk()
    val floorService: FloorService = mockk()
    val repository: BuildingRepository = mockk()
    val buildingService = BuildingService(fileService, facilityService, floorService, repository)

    Given("Building 생성을 진행할 때") {
        When("유효한 요청으로 Building 생성 요청") {
            val createRequest = dummyCreateBuildingRequest()
            val saved = dummyBuilding()

            every { facilityService.save(any<Facility>(), createRequest.facility) } returns saved
            every { floorService.save(any(), any()) } just runs

            Then("성공") {
                val saveId = buildingService.save(createRequest)
                saveId shouldBe saved.id
            }
        }
    }

    Given("Building 목록 조회를 진행할 때") {
        When("정상 요청이 오면") {
            val building = dummyBuilding()
            every {
                repository.findAll(any<Sort>())
            } returns mutableListOf(building)

            every {
                fileService.getFiles(any())
            } returns mutableListOf(dummyFileResponse())

            every {
                floorService.findAllByFacilities(any<List<Facility>>())
            } returns mapOf(building to mutableListOf(dummyFloorResponse()))

            Then("정상 조회") {
                buildingService.findAll().size shouldBe 1
            }
        }
    }

    Given("Building 상세 조회를 진행할 때") {
        When("유효한 아이디로 조회 요청") {
            val building = dummyBuilding()
            every {
                facilityService.findById(any())
            } returns building

            every {
                floorService.findAllByFacility(any())
            } returns mutableListOf(dummyFloorResponse())

            every {
                fileService.getFileResponse(any<Long>())
            } returns dummyFileResponse()

            Then("정상 조회") {
                val res = buildingService.findById(building.id)
                res.facility.name shouldBe building.name
                res.floors?.size shouldBe 1
            }
        }

        When("없는 아이디로 조회 요청") {
            Then("NOT_FOUND_FACILITY 예외 발생") {
                val searchId = 1L
                every { facilityService.findById(searchId) } throws
                    CustomException(ErrorCode.NOT_FOUND_FACILITY, searchId)

                shouldThrowExactly<CustomException> {
                    buildingService.findById(searchId)
                }.message shouldBe ErrorCode.NOT_FOUND_FACILITY.message.format(searchId)
            }
        }
    }

    Given("Building 수정을 진행할 때") {
        When("정상 수정 요청") {
            val updateRequest = dummyUpdateBuildingRequest()
            val building = dummyBuilding(name = updateRequest.facility.name)
            every {
                repository.findById(any())
            } returns Optional.of(building)

            every {
                facilityService.putUpdate(any(), any())
            } just runs

            every {
                floorService.update(any(), any())
            } just runs

            Then("정상 수정") {
                buildingService.putUpdate(building.id, updateRequest)
                building.name shouldBe updateRequest.facility.name
            }
        }
    }

    Given("Building 삭제를 진행할 때") {
        When("정상 삭제 요청") {
            val building = dummyBuilding()
            every {
                facilityService.findById(any())
            } returns building
            val slot = slot<Long>()
            every {
                floorService.delete(any())
            } just runs
            every {
                facilityService.deleteFacility(capture(slot))
            } just runs

            Then("정상 삭제") {
                buildingService.delete(building.id)
                verify(exactly = 1) { facilityService.deleteFacility(any()) }
                slot.captured shouldBe building.id
            }
        }
    }
})
