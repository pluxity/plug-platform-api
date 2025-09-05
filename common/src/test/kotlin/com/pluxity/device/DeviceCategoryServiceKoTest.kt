package com.pluxity.device

import com.pluxity.device.dto.DeviceCategoryRequest
import com.pluxity.device.dto.DeviceCategoryUpdateRequest
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.repository.DeviceCategoryRepository
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import device.dummyDevice
import device.dummyDeviceCategory
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

class DeviceCategoryServiceKoTest :
    BehaviorSpec({
        isolationMode = IsolationMode.InstancePerLeaf

        val deviceCategoryRepository: DeviceCategoryRepository = mockk()
        val deviceRepository: DeviceRepository = mockk()
        val fileService: FileService = mockk(relaxed = true)
        val facilityService: FacilityService = mockk()
        val jpaRepository: JpaRepository<DeviceCategory, Long> = mockk()

        val deviceCategoryService =
            DeviceCategoryService(
                deviceCategoryRepository,
                deviceRepository,
                fileService,
                facilityService,
                jpaRepository,
            )

        Given("디바이스 카테고리 생성을 진행할 때") {
            When("유효한 요청으로 카테고리 생성 요청") {
                val createRequest =
                    DeviceCategoryRequest(
                        name = "테스트 카테고리",
                        parentId = null,
                        thumbnailFileId = 1L,
                    )
                val savedCategory =
                    dummyDeviceCategory(name = createRequest.name).apply {
                        id = 1L
                    }

                every { jpaRepository.findByIdOrNull(any()) } returns null
                every { jpaRepository.save(any()) } returns savedCategory

                Then("성공") {
                    val result = deviceCategoryService.create(createRequest)
                    result shouldBe 1L
                }
            }

            When("부모 카테고리가 있는 요청으로 카테고리 생성 요청") {
                val parentCategory =
                    dummyDeviceCategory(name = "부모 카테고리").apply {
                        id = 10L
                    }
                val createRequest =
                    DeviceCategoryRequest(
                        name = "자식 카테고리",
                        parentId = 10L,
                        thumbnailFileId = null,
                    )
                val savedCategory =
                    dummyDeviceCategory(name = createRequest.name).apply {
                        id = 2L
                        parent = parentCategory
                    }

                every { jpaRepository.findByIdOrNull(10L) } returns parentCategory
                every { jpaRepository.save(any()) } returns savedCategory

                Then("성공") {
                    val result = deviceCategoryService.create(createRequest)
                    result shouldBe 2L
                }
            }
        }

        Given("디바이스 카테고리 조회를 진행할 때") {
            When("유효한 ID로 카테고리 조회 요청") {
                val category =
                    dummyDeviceCategory(name = "테스트 카테고리").apply {
                        id = 1L
                    }

                every { deviceCategoryRepository.findAllBy(any<Sort>()) } returns listOf(category)

                Then("정상 조회") {
                    val result = deviceCategoryService.getDeviceCategory(1L)
                    result shouldNotBe null
                    result.id shouldBe 1L
                    result.name shouldBe "테스트 카테고리"
                }
            }

            When("존재하지 않는 ID로 카테고리 조회 요청") {
                val category =
                    dummyDeviceCategory(name = "테스트 카테고리").apply {
                        id = 2L
                    }

                every { deviceCategoryRepository.findAllBy(any<Sort>()) } returns listOf(category)

                Then("NOT_FOUND_DEVICE_CATEGORY 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceCategoryService.getDeviceCategory(999L)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE_CATEGORY.getMessage().format(999L)
                }
            }
        }

        Given("디바이스 카테고리 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                val category1 = dummyDeviceCategory(name = "카테고리1").apply { id = 1L }
                val category2 = dummyDeviceCategory(name = "카테고리2").apply { id = 2L }

                every { deviceCategoryRepository.findAllBy(any<Sort>()) } returns listOf(category1, category2)

                Then("정상 조회") {
                    val result = deviceCategoryService.getDeviceCategories()
                    result.size shouldBe 2
                }
            }
        }

        Given("자식 디바이스 카테고리 조회를 진행할 때") {
            When("유효한 부모 ID로 자식 카테고리 조회 요청") {
                val parentCategory = dummyDeviceCategory(name = "부모 카테고리").apply { id = 1L }
                val childCategory =
                    dummyDeviceCategory(name = "자식 카테고리").apply {
                        id = 2L
                        parent = parentCategory
                    }

                every { deviceCategoryRepository.findByParentId(1L) } returns listOf(childCategory)

                Then("정상 조회") {
                    val result = deviceCategoryService.getChildDeviceCategories(1L)
                    result.size shouldBe 1
                    result[0].name shouldBe "자식 카테고리"
                }
            }
        }

        Given("디바이스 카테고리 수정을 진행할 때") {
            When("유효한 요청으로 카테고리 수정 요청") {
                val existingCategory =
                    dummyDeviceCategory(name = "기존 카테고리").apply {
                        id = 1L
                    }
                val updateRequest =
                    DeviceCategoryUpdateRequest(
                        name = "수정된 카테고리",
                        parentId = null,
                        thumbnailFileId = 2L,
                    )

                every { jpaRepository.findByIdOrNull(1L) } returns existingCategory

                Then("성공") {
                    deviceCategoryService.update(1L, updateRequest)

                    existingCategory.name shouldBe "수정된 카테고리"
                    existingCategory.iconFileId shouldBe 2L
                }
            }

            When("존재하지 않는 ID로 카테고리 수정 요청") {
                val updateRequest =
                    DeviceCategoryUpdateRequest(
                        name = "수정된 카테고리",
                        parentId = null,
                        thumbnailFileId = null,
                    )

                every { jpaRepository.findByIdOrNull(999L) } returns null

                Then("예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceCategoryService.update(999L, updateRequest)
                    }
                }
            }
        }

        Given("디바이스 카테고리 삭제를 진행할 때") {
            When("자식이 없고 디바이스가 없는 카테고리 삭제 요청") {
                val category =
                    dummyDeviceCategory(name = "삭제할 카테고리").apply {
                        id = 1L
                    }

                every { jpaRepository.findByIdOrNull(1L) } returns category
                every { deviceCategoryRepository.delete(any()) } just runs

                Then("성공") {
                    deviceCategoryService.delete(1L)
                    verify { deviceCategoryRepository.delete(category) }
                }
            }

            When("자식이 있는 카테고리 삭제 요청") {
                val parentCategory = dummyDeviceCategory(name = "부모 카테고리").apply { id = 1L }
                val childCategory =
                    dummyDeviceCategory(name = "자식 카테고리").apply {
                        id = 2L
                        parent = parentCategory
                    }
                parentCategory.children.add(childCategory)

                every { jpaRepository.findByIdOrNull(1L) } returns parentCategory

                Then("CATEGORY_HAS_CHILDREN 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceCategoryService.delete(1L)
                    }.message shouldBe ErrorCode.CATEGORY_HAS_CHILDREN.getMessage()
                }
            }

            When("디바이스가 있는 카테고리 삭제 요청") {
                val category = dummyDeviceCategory(name = "카테고리").apply { id = 1L }
                val device: Device = mockk(relaxed = true)
                category.devices.add(device)

                every { jpaRepository.findByIdOrNull(1L) } returns category

                Then("CATEGORY_HAS_DEVICES 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        deviceCategoryService.delete(1L)
                    }.message shouldBe ErrorCode.CATEGORY_HAS_DEVICES.getMessage()
                }
            }
        }

        Given("카테고리별 디바이스 조회를 진행할 때") {
            When("유효한 카테고리 ID와 시설 ID로 조회 요청") {
                val category = dummyDeviceCategory(name = "카테고리", iconFileId = 1L).apply { id = 1L }
                val facility: Facility = mockk(relaxed = true)
                val device: Device = dummyDevice(id = "device-001", category = category)

                every { jpaRepository.findByIdOrNull(1L) } returns category
                every { facilityService.findById(10L) } returns facility
                every { deviceRepository.findByCategoryAndFacility(category, facility) } returns listOf(device)

                Then("정상 조회") {
                    val result = deviceCategoryService.getDevicesByCategoryId(1L, 10L)
                    result shouldNotBe null
                }
            }
        }

        Given("디바이스 카테고리 깊이 조회를 진행할 때") {
            When("카테고리 깊이 조회 요청") {
                Then("성공") {
                    val result = deviceCategoryService.getDeviceCategoryDepth()
                    result shouldNotBe null
                    result.maxDepth shouldNotBe null
                }
            }
        }
    })
