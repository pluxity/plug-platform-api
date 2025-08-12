package com.pluxity.device

import com.pluxity.cctv.CctvService
import com.pluxity.cctv.repository.DeviceCctvRepository
import com.pluxity.device.dto.GsDeviceCreateRequest
import com.pluxity.device.dto.GsDeviceUpdateRequest
import com.pluxity.device.entity.dummyGsDevice
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.exception.CustomException
import com.pluxity.global.response.BaseResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.util.*

class GsDeviceServiceKoTest : BehaviorSpec({
    val repository: GsDeviceRepository = mockk()
    val deviceCategoryService: DeviceCategoryService = mockk()
    val deviceCctvRepository: DeviceCctvRepository = mockk()
    val cctvService: CctvService = mockk()
    val fileService: FileService = mockk()
    val gsDeviceService =
        GsDeviceService(repository, deviceCategoryService, deviceCctvRepository, cctvService, fileService)

    Given("디바이스 생성을 진행할 때") {
        When("유효한 요청으로 GS 디바이스 생성 요청") {
            val id = UUID.randomUUID().toString()
            val createRequest =
                GsDeviceCreateRequest(id, "Test Device", null)

            every {
                repository.save(any())
            } returns dummyGsDevice(id = id)
            Then("성공") {
                val saveId = gsDeviceService.save(createRequest)
                saveId shouldBe createRequest.id
            }
        }
    }

    Given("디바이스 목록 조회를 진행할 때") {
        When("정상 요청이 오면") {
            every {
                repository.findAll()
            } returns mutableListOf(dummyGsDevice())

            every {
                fileService.getFiles(any())
            } returns mutableListOf(FileResponse(1L, "url", "", "", "", BaseResponse("", "", "", "")))

            Then("정상 조회") {
                gsDeviceService.findAll().size shouldBe 1
            }
        }
    }

    Given("디바이스 상세 조회를 진행할 때") {
        When("유효한 아이디로 조회 요청") {
            val device = dummyGsDevice()
            every {
                repository.findById(any())
            } returns Optional.of(device)
            val res = gsDeviceService.findById(device.id)
            Then("정상 조회") {
                res.id shouldBe device.id
                res.name shouldBe device.name
            }
        }

        When("없는 아이디로 조회 요청") {
            every {
                repository.findById(any())
            } returns Optional.empty()
            Then("NOT_FOUND_DEVICE 예외 발생") {
                val searchId = UUID.randomUUID().toString();
                shouldThrow<CustomException> {
                    gsDeviceService.findById(searchId)
                }.message shouldBe "ID가 ${searchId}인 디바이스를 찾을 수 없습니다."
            }
        }
    }

    Given("디바이스 수정을 진행할 때") {
        When("정상 수정 요청") {
            val device = dummyGsDevice()
            every {
                repository.findById(any())
            } returns Optional.of(device)
            val updateName = "updated Device"
            gsDeviceService.putUpdate(device.id, GsDeviceUpdateRequest(updateName, null))
            Then("정상 수정") {
                device.name shouldBe updateName
            }
        }
    }

    Given("디바이스 삭제를 진행할 때") {
        When("정상 삭제 요청") {
            val device = dummyGsDevice()
            every {
                repository.findById(any())
            } returns Optional.of(device)
            every {
                deviceCctvRepository.deleteByDevice(any())
            } just runs
            every {
                repository.delete(any())
            } just runs
            gsDeviceService.delete(device.id)
            Then("정상 삭제") {
                verify(exactly = 1) { repository.delete(device) }
            }
        }
    }
})
