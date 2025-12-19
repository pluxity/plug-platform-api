package com.pluxity.device

import com.pluxity.device.dto.DeviceCreateRequest
import com.pluxity.device.dto.DeviceUpdateRequest
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.device.service.DeviceService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import device.dummyDevice
import device.dummyDeviceCategory
import file.dummyFileResponse
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify

class DeviceServiceKoTest :
    BehaviorSpec({
        isolationMode = IsolationMode.InstancePerLeaf

        val deviceRepository: DeviceRepository = mockk()
        val fileService: FileService = mockk(relaxed = true)
        val deviceCategoryService: DeviceCategoryService = mockk()

        val deviceService =
            DeviceService(
                deviceRepository,
                fileService,
                deviceCategoryService,
            )

        Given("Device 생성을 진행할 때") {
            When("카테고리 없이 유효한 요청으로 Device 생성 요청") {
                val createRequest =
                    DeviceCreateRequest(
                        id = "device-001",
                        name = "테스트 디바이스",
                        categoryId = null,
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )
                val savedDevice =
                    dummyDevice(
                        id = createRequest.id,
                        name = createRequest.name,
                        deviceType = createRequest.deviceType,
                        companyType = createRequest.companyType,
                    )

                every { deviceRepository.save(any()) } returns savedDevice

                Then("성공") {
                    val result = deviceService.save(createRequest)
                    result shouldBe savedDevice.id
                }
            }

            When("카테고리와 함께 유효한 요청으로 Device 생성 요청") {
                val categoryId = 1L
                val category = dummyDeviceCategory()
                val createRequest =
                    DeviceCreateRequest(
                        id = "device-002",
                        name = "테스트 디바이스 2",
                        categoryId = categoryId,
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )
                val savedDevice =
                    dummyDevice(
                        id = createRequest.id,
                        name = createRequest.name,
                        category = category,
                        deviceType = createRequest.deviceType,
                        companyType = createRequest.companyType,
                    )

                every { deviceCategoryService.findById(categoryId) } returns category
                every { deviceRepository.save(any()) } returns savedDevice

                Then("성공") {
                    val result = deviceService.save(createRequest)
                    result shouldBe savedDevice.id
                }
            }
        }

        Given("Device 상세 조회를 진행할 때") {
            When("유효한 아이디로 조회 요청") {
                val deviceId = "device-001"
                val device =
                    dummyDevice(
                        id = deviceId,
                        name = "테스트 디바이스",
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )

                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns device

                Then("정상 조회") {
                    val result = deviceService.findById(deviceId)
                    result.id shouldBe device.id
                    result.name shouldBe device.name
                }
            }

            When("카테고리가 있는 디바이스 조회 요청") {
                val deviceId = "device-002"
                val category = dummyDeviceCategory(iconFileId = 1L)
                val device =
                    dummyDevice(
                        id = deviceId,
                        name = "테스트 디바이스 2",
                        category = category,
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )
                val fileResponse =
                    dummyFileResponse(
                        1L,
                        "test-file",
                        "test-path",
                    )
                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns device
                checkNotNull(category.iconFileId)
                every { fileService.getFileResponse(category.iconFileId) } returns fileResponse

                Then("파일 정보와 함께 정상 조회") {
                    val result = deviceService.findById(deviceId)
                    result.id shouldBe device.id
                    result.name shouldBe device.name
                    result.deviceCategory?.thumbnailFile shouldBe fileResponse
                }
            }

            When("없는 아이디로 조회 요청") {
                val deviceId = "not-found"
                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns null

                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceService.findById(deviceId)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(deviceId)
                }
            }
        }

        Given("Device 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                val device1 =
                    dummyDevice(
                        id = "device-001",
                        name = "디바이스 1",
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )
                val device2 =
                    dummyDevice(
                        id = "device-002",
                        name = "디바이스 2",
                        category = dummyDeviceCategory(iconFileId = 1L),
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )
                val devices = listOf(device1, device2)

                every {
                    fileService.getFiles(any())
                } returns mutableListOf(dummyFileResponse())

                every { deviceRepository.findAllByFacilityIdIfPresent(any()) } returns devices

                Then("정상 조회") {
                    val result = deviceService.findAll()
                    result.size shouldBe 2
                    result[0].id shouldBe device1.id
                    result[1].id shouldBe device2.id
                }
            }
        }

        Given("Device 타입 목록 조회를 진행할 때") {
            When("요청이 오면") {
                Then("모든 디바이스 타입 반환") {
                    val result = deviceService.findAllType()
                    result.isNotEmpty() shouldBe true
                }
            }
        }

        Given("Device 회사 타입 목록 조회를 진행할 때") {
            When("요청이 오면") {
                Then("모든 회사 타입 반환") {
                    val result = deviceService.findAllCompanyType()
                    result.isNotEmpty() shouldBe true
                }
            }
        }

        Given("Device 업데이트를 진행할 때") {
            When("유효한 요청으로 업데이트") {
                val deviceId = "device-001"
                val device =
                    dummyDevice(
                        id = deviceId,
                        name = "기존 이름",
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )
                val updateRequest =
                    DeviceUpdateRequest(
                        name = "새로운 이름",
                        categoryId = 1L,
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )
                val category = dummyDeviceCategory()

                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns device
                every { deviceCategoryService.findById(1L) } returns category

                Then("성공") {
                    deviceService.putUpdate(deviceId, updateRequest)
                    verify { deviceCategoryService.findById(1L) }
                }
            }

            When("없는 아이디로 업데이트 요청") {
                val deviceId = "not-found"
                val updateRequest =
                    DeviceUpdateRequest(
                        name = "새로운 이름",
                        categoryId = null,
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )

                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns null

                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceService.putUpdate(deviceId, updateRequest)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(deviceId)
                }
            }
        }

        Given("Device 삭제를 진행할 때") {
            When("유효한 아이디로 삭제 요청") {
                val deviceId = "device-001"
                val device =
                    dummyDevice(
                        id = deviceId,
                        name = "테스트 디바이스",
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )

                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns device
                every { deviceRepository.deleteById(deviceId) } just runs

                Then("성공") {
                    deviceService.delete(deviceId)
                    verify { deviceRepository.deleteById(deviceId) }
                }
            }

            When("없는 아이디로 삭제 요청") {
                val deviceId = "not-found"
                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns null

                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceService.delete(deviceId)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(deviceId)
                }
            }
        }

        Given("Device에 카테고리 할당을 진행할 때") {
            When("유효한 요청으로 카테고리 할당") {
                val deviceId = "device-001"
                val categoryId = 1L
                val device =
                    dummyDevice(
                        id = deviceId,
                        name = "테스트 디바이스",
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )
                val category = dummyDeviceCategory()

                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns device
                every { deviceCategoryService.findById(categoryId) } returns category

                Then("성공") {
                    deviceService.assignCategory(deviceId, categoryId)
                    verify { deviceCategoryService.findById(categoryId) }
                }
            }

            When("없는 디바이스에 카테고리 할당 요청") {
                val deviceId = "not-found"
                val categoryId = 1L
                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns null

                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceService.assignCategory(deviceId, categoryId)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(deviceId)
                }
            }
        }

        Given("Device에서 카테고리 제거를 진행할 때") {
            When("카테고리가 할당된 디바이스에서 카테고리 제거 요청") {
                val deviceId = "device-001"
                val category = dummyDeviceCategory()
                val device =
                    dummyDevice(
                        id = deviceId,
                        name = "테스트 디바이스",
                        category = category,
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )

                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns device

                Then("성공") {
                    deviceService.removeCategory(deviceId)
                }
            }

            When("카테고리가 할당되지 않은 디바이스에서 카테고리 제거 요청") {
                val deviceId = "device-001"
                val device =
                    dummyDevice(
                        id = deviceId,
                        name = "테스트 디바이스",
                        category = null,
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    )

                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns device

                Then("NOT_FOUND_ASSIGN_DEVICE_CATEGORY 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceService.removeCategory(deviceId)
                    }.message shouldBe ErrorCode.NOT_FOUND_ASSIGN_DEVICE_CATEGORY.getMessage().format(deviceId)
                }
            }

            When("없는 디바이스에서 카테고리 제거 요청") {
                val deviceId = "not-found"
                every { deviceRepository.findByIdOrNullCustom(deviceId) } returns null

                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceService.removeCategory(deviceId)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(deviceId)
                }
            }
        }
    })
