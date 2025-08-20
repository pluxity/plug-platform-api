package com.pluxity.device

import cctv.dummyCctv
import com.pluxity.cctv.CctvService
import com.pluxity.cctv.entity.DeviceCctv
import com.pluxity.cctv.repository.DeviceCctvRepository
import com.pluxity.device.dto.GsDeviceCctvUpdateRequest
import com.pluxity.device.dto.GsDeviceUpdateRequest
import com.pluxity.device.dto.dummyCreateGsDeviceRequest
import com.pluxity.device.entity.dummyDeviceCctv
import com.pluxity.device.entity.dummyGsDevice
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

class GsDeviceServiceKoTest :
    BehaviorSpec({
        val repository: GsDeviceRepository = mockk()
        val deviceCategoryService: DeviceCategoryService = mockk()
        val deviceCctvRepository: DeviceCctvRepository = mockk()
        val cctvService: CctvService = mockk()
        val fileService: FileService = mockk()
        val gsDeviceService = GsDeviceService(repository, deviceCategoryService, deviceCctvRepository, cctvService, fileService)

        Given("디바이스 생성을 진행할 때") {
            When("유효한 요청으로 GS 디바이스 생성 요청") {
                val id = UUID.randomUUID().toString()
                val createRequest = dummyCreateGsDeviceRequest().copy(id = id)

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
                } returns mutableListOf(dummyFileResponse())

                Then("정상 조회") {
                    gsDeviceService.findAll().size shouldBe 1
                }
            }
        }

        Given("디바이스 상세 조회를 진행할 때") {
            When("유효한 아이디로 조회 요청") {
                val device = dummyGsDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                val res = gsDeviceService.findById(device.id)
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
                        gsDeviceService.findById(searchId)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.message.format(searchId)
                }
            }
        }

        Given("디바이스 수정을 진행할 때") {
            When("정상 수정 요청") {
                val device = dummyGsDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device
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
                val slot = slot<String>()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                every {
                    deviceCctvRepository.deleteByDevice(any())
                } just runs
                every {
                    repository.deleteById(capture(slot))
                } just runs
                gsDeviceService.delete(device.id)
                Then("정상 삭제") {
                    verify(exactly = 1) { repository.deleteById(any()) }
                    slot.captured shouldBe device.id
                }
            }
        }

        Given("디바이스에 카테고리를 할당할 때") {
            When("정상 할당 요청") {
                val device = dummyGsDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                val category = dummyDeviceCategory()
                every {
                    deviceCategoryService.findById(any())
                } returns category
                gsDeviceService.assignCategory(device.id, 1)
                Then("정상 할당") {
                    device.category.name shouldBe category.name
                }
            }
        }

        Given("디바이스의 카테고리를 제거할 때") {
            When("정상 제거 요청") {
                val device = dummyGsDevice(category = dummyDeviceCategory())
                every {
                    repository.findByIdOrNull(any())
                } returns device
                gsDeviceService.removeCategory(device.id)
                Then("정상 제거") {
                    device.category shouldBe null
                }
            }

            When("카테고리가 없는 디바이스에서 제거 요청") {
                val device = dummyGsDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device
                Then("정상 제거") {
                    shouldThrowExactly<CustomException> {
                        gsDeviceService.removeCategory(device.id)
                    }.message shouldBe ErrorCode.NOT_FOUND_ASSIGN_DEVICE_CATEGORY.message.format(device.id)
                }
            }
        }

        Given("디바이스에 연결된 CCTV 조회할 때") {
            When("정상 조회 요청") {
                val device = dummyGsDevice()
                every {
                    repository.findByIdOrNull(any())
                } returns device

                every {
                    deviceCctvRepository.findByDevice(any())
                } returns listOf(dummyDeviceCctv())

                every {
                    fileService.getFileResponse(any<Long>())
                } returns dummyFileResponse()

                Then("정상 조회") {
                    gsDeviceService.getCctvByDeviceId(device.id).size shouldBe 1
                }
            }
        }

        Given("디바이스에 CCTV를 설정할 때") {
            val device = dummyGsDevice(id = "dev1")
            val exist =
                listOf(
                    dummyDeviceCctv(dummyCctv(id = "A"), device),
                    dummyDeviceCctv(dummyCctv(id = "B"), device),
                )
            // B는 유지, C는 추가, A는 제거 대상
            val req =
                GsDeviceCctvUpdateRequest(
                    cctvIds = mutableListOf("B", "C"),
                )
            every { repository.findByIdOrNull(any()) } returns device
            every { deviceCctvRepository.findByDevice(device) } returns exist
            every { cctvService.findById("C") } returns dummyCctv(id = "C")

            val savedArg = slot<List<DeviceCctv>>()
            every { deviceCctvRepository.saveAll(capture(savedArg)) } answers { savedArg.captured }
            val removedArg = slot<List<String>>()
            every { deviceCctvRepository.deleteByCctvIdIn(capture(removedArg)) } just runs

            When("요청 목록으로 업데이트를 호출하면") {
                gsDeviceService.assignCctvToDevice(deviceId = "dev1", request = req)

                Then("saveAll은 C만 저장하고, deleteByCctvIdIn은 A만 삭제한다") {
                    // saveAll 검증: C만 들어갔는지
                    verify(exactly = 1) { deviceCctvRepository.saveAll(any<List<DeviceCctv>>()) }
                    savedArg.captured.map { it.cctv.id } shouldBe listOf("C")

                    // delete 검증: A만 들어갔는지
                    verify(exactly = 1) { deviceCctvRepository.deleteByCctvIdIn(any()) }
                    removedArg.captured shouldBe listOf("A")
                }
            }
        }
    })
