package com.pluxity.park

import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.park.dto.dummyCreateParkRequest
import com.pluxity.park.dto.dummyUpdateParkRequest
import com.pluxity.park.entity.dummyPark
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
import org.springframework.data.repository.findByIdOrNull

class ParkServiceKoTest :
    BehaviorSpec({
        val fileService: FileService = mockk()
        val facilityService: FacilityService = mockk()
        val parkRepository: ParkRepository = mockk()
        val parkService = ParkService(fileService, facilityService, parkRepository)

        Given("Park 생성을 진행할 때") {
            When("유효한 요청으로 Park 생성 요청") {
                val createRequest = dummyCreateParkRequest()
                val saved = dummyPark()

                every { facilityService.save(any<Facility>(), createRequest.facility) } returns saved

                Then("성공") {
                    val saveId = parkService.save(createRequest)
                    saveId shouldBe saved.id
                }
            }
        }

        Given("Park 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                val park = dummyPark()
                every {
                    parkRepository.findAll(any<Sort>())
                } returns mutableListOf(park)

                every {
                    fileService.getFiles(any())
                } returns mutableListOf(dummyFileResponse())

                Then("정상 조회") {
                    parkService.findAll().size shouldBe 1
                }
            }
        }

        Given("Park 상세 조회를 진행할 때") {
            When("유효한 아이디로 조회 요청") {
                val park = dummyPark()
                every {
                    facilityService.findById(any())
                } returns park

                every {
                    fileService.getFileResponse(any<Long>())
                } returns dummyFileResponse()

                Then("정상 조회") {
                    val res = parkService.findById(park.id!!)
                    res.facility.name shouldBe park.name
                }
            }

            When("없는 아이디로 조회 요청") {
                Then("NOT_FOUND_FACILITY 예외 발생") {
                    val searchId = 1L
                    every { facilityService.findById(searchId) } throws
                        CustomException(ErrorCode.NOT_FOUND_FACILITY, searchId)

                    shouldThrowExactly<CustomException> {
                        parkService.findById(searchId)
                    }.message shouldBe ErrorCode.NOT_FOUND_FACILITY.getMessage().format(searchId)
                }
            }
        }

        Given("Park 수정을 진행할 때") {
            When("정상 수정 요청") {
                val updateRequest = dummyUpdateParkRequest("updatedBoundary")
                val park = dummyPark(name = updateRequest.facility.name!!, boundary = updateRequest.boundary)
                every {
                    parkRepository.findByIdOrNull(any())
                } returns park

                every {
                    facilityService.putUpdate(any(), any())
                } just runs

                Then("정상 수정") {
                    parkService.update(park.id!!, updateRequest)
                    park.name shouldBe updateRequest.facility.name
                    park.boundary shouldBe updateRequest.boundary
                }
            }
        }

        Given("Park 삭제를 진행할 때") {
            When("정상 삭제 요청") {
                val park = dummyPark()
                every {
                    parkRepository.findByIdOrNull(any())
                } returns park
                val slot = slot<Long>()
                every {
                    facilityService.deleteFacility(capture(slot))
                } just runs

                Then("정상 삭제") {
                    parkService.delete(park.id!!)
                    verify(exactly = 1) { facilityService.deleteFacility(any()) }
                    slot.captured shouldBe park.id
                }
            }
        }
    })
