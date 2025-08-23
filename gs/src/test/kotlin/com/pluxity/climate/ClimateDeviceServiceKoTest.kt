package com.pluxity.climate

import com.pluxity.climate.dto.ClimateDeviceUpdateRequest
import com.pluxity.climate.dto.dummyClimateDeviceCreateRequest
import com.pluxity.climate.entity.dummyClimateDevice
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import device.dummyDeviceCategory
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
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class ClimateDeviceServiceKoTest :
    BehaviorSpec({
        val repository: ClimateDeviceRepository = mockk()
        val deviceCategoryService: DeviceCategoryService = mockk()
        val fileService: FileService = mockk()

        val climateDeviceService = ClimateDeviceService(repository, deviceCategoryService, fileService)

        Given("디바이스 생성을 진행할 때") {
            When("유효한 요청으로 GS 디바이스 생성 요청") {
                val id = UUID.randomUUID().toString()
                val createRequest = dummyClimateDeviceCreateRequest().copy(id = id)

                every {
                    repository.save(any())
                } returns dummyClimateDevice(id = id)
                Then("성공") {
                    val saveId = climateDeviceService.save(createRequest)
                    saveId shouldBe createRequest.id
                }
            }
        }

        Given("디바이스 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                every {
                    repository.findAll()
                } returns mutableListOf(dummyClimateDevice())

                every {
                    fileService.getFiles(any())
                } returns mutableListOf(dummyFileResponse())

                Then("정상 조회") {
                    climateDeviceService.findAll().size shouldBe 1
                }
            }
        }

        Given("디바이스 상세 조회를 진행할 때") {
            When("유효한 아이디로 조회 요청") {
                val device = dummyClimateDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                val res = climateDeviceService.findById(device.id)
                Then("정상 조회") {
                    res.id shouldBe device.id
                    res.name shouldBe device.name
                }
            }

            When("없는 아이디로 조회 요청") {
                every {
                    repository.findByIdOrNull(any())
                } returns null
                Then("NOT_FOUND_DEVICE 예외 발생") {
                    val searchId = UUID.randomUUID().toString()
                    shouldThrowExactly<CustomException> {
                        climateDeviceService.findById(searchId)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.message.format(searchId)
                }
            }
        }

        Given("디바이스 수정을 진행할 때") {
            When("정상 수정 요청") {
                val device = dummyClimateDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                val updateName = "updated Device"
                climateDeviceService.putUpdate(device.id, ClimateDeviceUpdateRequest(updateName, null))
                Then("정상 수정") {
                    device.name shouldBe updateName
                }
            }
        }

        Given("디바이스 삭제를 진행할 때") {
            When("정상 삭제 요청") {
                val device = dummyClimateDevice()
                val slot = slot<String>()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                every {
                    repository.deleteById(capture(slot))
                } just runs
                climateDeviceService.delete(device.id)
                Then("정상 삭제") {
                    verify(exactly = 1) { repository.deleteById(any()) }
                    slot.captured shouldBe device.id
                }
            }
        }

        Given("디바이스에 카테고리를 할당할 때") {
            When("정상 할당 요청") {
                val device = dummyClimateDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                val category = dummyDeviceCategory()
                every {
                    deviceCategoryService.findById(any())
                } returns category
                climateDeviceService.assignCategory(device.id, 1)
                Then("정상 할당") {
                    device.category.name shouldBe category.name
                }
            }
        }

        Given("디바이스의 카테고리를 제거할 때") {
            When("정상 제거 요청") {
                val device = dummyClimateDevice(category = dummyDeviceCategory())
                every {
                    repository.findByIdOrNull(any())
                } returns device
                climateDeviceService.removeCategory(device.id)
                Then("정상 제거") {
                    device.category shouldBe null
                }
            }

            When("카테고리가 없는 디바이스에서 제거 요청") {
                val device = dummyClimateDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                Then("정상 제거") {
                    shouldThrowExactly<CustomException> {
                        climateDeviceService.removeCategory(device.id)
                    }.message shouldBe ErrorCode.NOT_FOUND_ASSIGN_DEVICE_CATEGORY.message.format(device.id)
                }
            }
        }
    })
