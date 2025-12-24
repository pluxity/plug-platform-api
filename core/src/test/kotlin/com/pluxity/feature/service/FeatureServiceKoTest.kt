
package com.pluxity.feature.service

import com.pluxity.asset.service.AssetValidator
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.feature.dto.FeatureAssignDto
import com.pluxity.feature.dto.FeatureCreateRequest
import com.pluxity.feature.dto.FeatureUpdateRequest
import com.pluxity.feature.entity.Feature
import com.pluxity.feature.entity.Spatial
import com.pluxity.feature.repository.FeatureRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import device.dummyDevice
import entity.dummyFeature
import entity.dummySpatial
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

class FeatureServiceKoTest :
    BehaviorSpec({
        val featureRepository: FeatureRepository = mockk()
        val facilityService: FacilityService = mockk()
        val assetValidator: AssetValidator = mockk()
        val deviceRepository: DeviceRepository = mockk()
        val featureAssignment: FeatureAssignment = mockk(relaxed = true)

        val featureService =
            FeatureService(
                featureRepository,
                facilityService,
                assetValidator,
                deviceRepository,
                featureAssignment,
            )

        Given("Feature 생성을 진행할 때") {
            When("유효한 요청으로 Feature 생성 요청") {
                val createRequest =
                    FeatureCreateRequest(
                        id = "test-feature-id",
                        position = dummySpatial(1.0, 2.0, 3.0),
                        rotation = dummySpatial(0.1, 0.2, 0.3),
                        scale = dummySpatial(1.0, 1.0, 1.0),
                        assetId = 1L,
                        facilityId = 1L,
                        floorId = "floor-1",
                    )
                val facility = mockk<Facility>()
                val savedFeature =
                    dummyFeature(
                        id = createRequest.id,
                        position = createRequest.position,
                        rotation = createRequest.rotation,
                        scale = createRequest.scale,
                        assetId = createRequest.assetId,
                        facility = facility,
                        floorId = createRequest.floorId,
                    )

                every { featureRepository.findByIdOrNull(createRequest.id) } returns null
                every { facilityService.findById(createRequest.facilityId) } returns facility
                every { assetValidator.validateAssetId(createRequest.assetId) } just runs
                every { featureRepository.save(any<Feature>()) } returns savedFeature

                Then("성공") {
                    val response = featureService.createFeature(createRequest)
                    response.id shouldBe createRequest.id
                }
            }

            When("중복된 ID로 Feature 생성 요청") {
                val createRequest =
                    FeatureCreateRequest(
                        id = "existing-feature-id",
                        position = null,
                        rotation = null,
                        scale = null,
                        assetId = 1L,
                        facilityId = 1L,
                        floorId = null,
                    )
                val existingFeature = dummyFeature(id = createRequest.id)

                every { featureRepository.findByIdOrNull(createRequest.id) } returns existingFeature

                Then("DUPLICATE_FEATURE_ID 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.createFeature(createRequest)
                    }.message shouldBe ErrorCode.DUPLICATE_FEATURE_ID.getMessage().format(createRequest.id)
                }
            }

            When("기본값으로 Feature 생성 요청") {
                val createRequest =
                    FeatureCreateRequest(
                        id = "test-feature-id",
                        position = null,
                        rotation = null,
                        scale = null,
                        assetId = 1L,
                        facilityId = 1L,
                        floorId = null,
                    )
                val facility = mockk<Facility>()
                val savedFeature =
                    dummyFeature(
                        id = createRequest.id,
                        position = Spatial(0.0, 0.0, 0.0),
                        rotation = Spatial(0.0, 0.0, 0.0),
                        scale = Spatial(1.0, 1.0, 1.0),
                        facility = facility,
                    )

                every { featureRepository.findByIdOrNull(createRequest.id) } returns null
                every { facilityService.findById(createRequest.facilityId) } returns facility
                every { assetValidator.validateAssetId(createRequest.assetId) } just runs
                every { featureRepository.save(any<Feature>()) } returns savedFeature

                Then("기본값으로 성공") {
                    val response = featureService.createFeature(createRequest)
                    response.id shouldBe createRequest.id
                }
            }
        }

        Given("Feature 목록 조회를 진행할 때") {
            When("유효한 facilityId로 조회 요청") {
                val facilityId = 1L
                val facility = mockk<Facility>()
                val features =
                    listOf(
                        dummyFeature(id = "feature-1", facility = facility),
                        dummyFeature(id = "feature-2", facility = facility),
                    )

                every { facilityService.findById(facilityId) } returns facility
                every { featureRepository.findByFacilityOrderByCreatedAtDesc(facility) } returns features

                Then("정상 조회") {
                    val response = featureService.getFeatures(facilityId)
                    response.size shouldBe 2
                    response[0].id shouldBe "feature-1"
                    response[1].id shouldBe "feature-2"
                }
            }

            When("존재하지 않는 facilityId로 조회 요청") {
                val facilityId = 999L

                every { facilityService.findById(facilityId) } throws CustomException(ErrorCode.NOT_FOUND_FACILITY, facilityId)

                Then("NOT_FOUND_FACILITY 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.getFeatures(facilityId)
                    }.message shouldBe ErrorCode.NOT_FOUND_FACILITY.getMessage().format(facilityId)
                }
            }
        }

        Given("Feature 수정을 진행할 때") {
            When("유효한 요청으로 수정") {
                val featureId = "test-feature-id"
                val updateRequest =
                    FeatureUpdateRequest(
                        position = dummySpatial(2.0, 3.0, 4.0),
                        rotation = dummySpatial(0.2, 0.3, 0.4),
                        scale = dummySpatial(1.5, 1.5, 1.5),
                    )
                val existingFeature = dummyFeature(id = featureId)
                val updatedFeature =
                    dummyFeature(
                        id = featureId,
                        position = updateRequest.position,
                        rotation = updateRequest.rotation,
                        scale = updateRequest.scale,
                    )

                every { featureRepository.findByIdOrNull(featureId) } returns existingFeature
                every { featureRepository.save(any<Feature>()) } returns updatedFeature

                Then("성공") {
                    val response = featureService.updateFeature(featureId, updateRequest)
                    response.id shouldBe featureId
                    response.position shouldBe updateRequest.position
                }
            }

            When("존재하지 않는 Feature ID로 수정 요청") {
                val featureId = "non-existing-feature"
                val updateRequest =
                    FeatureUpdateRequest(
                        position = dummySpatial(2.0, 3.0, 4.0),
                        rotation = dummySpatial(0.2, 0.3, 0.4),
                        scale = dummySpatial(1.5, 1.5, 1.5),
                    )

                every { featureRepository.findByIdOrNull(featureId) } returns null

                Then("NOT_FOUND_FEATURE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.updateFeature(featureId, updateRequest)
                    }.message shouldBe ErrorCode.NOT_FOUND_FEATURE.getMessage().format(featureId)
                }
            }
        }

        Given("Feature 삭제를 진행할 때") {
            When("유효한 Feature ID로 삭제 요청") {
                val featureId = "test-feature-id"
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.revokeByFeature(feature) } just runs
                every { featureRepository.delete(feature) } just runs

                Then("성공") {
                    featureService.deleteFeature(featureId)
                    verify { deviceRepository.revokeByFeature(feature) }
                    verify { featureRepository.delete(feature) }
                }
            }

            When("존재하지 않는 Feature ID로 삭제 요청") {
                val featureId = "non-existing-feature"

                every { featureRepository.findByIdOrNull(featureId) } returns null

                Then("NOT_FOUND_FEATURE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.deleteFeature(featureId)
                    }.message shouldBe ErrorCode.NOT_FOUND_FEATURE.getMessage().format(featureId)
                }
            }
        }

        Given("Feature ID로 조회할 때") {
            When("유효한 Feature ID로 조회") {
                val featureId = "test-feature-id"
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature

                Then("성공") {
                    val response = featureService.findFeatureById(featureId)
                    response.id shouldBe featureId
                }
            }

            When("존재하지 않는 Feature ID로 조회") {
                val featureId = "non-existing-feature"

                every { featureRepository.findByIdOrNull(featureId) } returns null

                Then("NOT_FOUND_FEATURE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.findFeatureById(featureId)
                    }.message shouldBe ErrorCode.NOT_FOUND_FEATURE.getMessage().format(featureId)
                }
            }
        }

        Given("AssetId로 Feature ID 목록 조회할 때") {
            When("유효한 AssetId로 조회") {
                val assetId = 1L
                val features = listOf(dummyFeature(id = "feature-1", assetId = assetId), dummyFeature(id = "feature-2", assetId = assetId))

                every { featureRepository.findByAssetId(assetId) } returns features

                Then("성공") {
                    val response = featureService.findFeatureIdsByAssetId(assetId)
                    response[0] shouldBe "feature-1"
                    response[1] shouldBe "feature-2"
                }
            }
        }

        Given("Feature에 Device 할당할 때") {
            When("유효한 요청으로 Device 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.DEVICE)
                val feature = dummyFeature(id = featureId)
                val device = dummyDevice(id = "device-1")

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.findByIdOrNull(assignDto.id) } returns device
                every { deviceRepository.existsByFeature(feature) } returns false
                every { deviceRepository.revokeByFeature(any()) } just runs

                Then("성공") {
                    featureService.assignSomethingToFeature(featureId, assignDto, false)
                    device.feature shouldBe feature
                }
            }

            When("이미 할당된 Device가 있는 Feature에 강제 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.DEVICE)
                val feature = dummyFeature(id = featureId)
                val device = dummyDevice(id = "device-1", feature = dummyFeature("exist-feature"))

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.findByIdOrNull(assignDto.id) } returns device
                every { deviceRepository.existsByFeature(feature) } returns true
                every { deviceRepository.revokeByFeature(any()) } just runs

                Then("성공") {
                    featureService.assignSomethingToFeature(featureId, assignDto, true)
                    device.feature shouldBe feature
                }
            }

            When("이미 할당된 Device가 있는 Feature에 강제하지 않고 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.DEVICE)
                val feature = dummyFeature(id = featureId)
                val device = dummyDevice(id = "device-1")

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.findByIdOrNull(assignDto.id) } returns device
                every { deviceRepository.existsByFeature(feature) } returns true

                Then("ALREADY_FEATURE_ASSIGNED 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }.message shouldBe ErrorCode.ALREADY_FEATURE_ASSIGNED.getMessage().format(featureId)
                }
            }

            When("이미 다른 Feature에 할당된 Device 할당 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.DEVICE)
                val feature = dummyFeature(id = featureId)
                val device = dummyDevice(id = "device-1", feature = dummyFeature("exist-feature"))

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.findByIdOrNull(assignDto.id) } returns device
                every { deviceRepository.existsByFeature(feature) } returns false

                Then("DUPLICATE_DEVICE_OTHER_FEATURE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }.message shouldBe ErrorCode.DUPLICATE_DEVICE_OTHER_FEATURE.getMessage().format(assignDto.id)
                }
            }

            When("존재하지 않는 Device 할당 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.DEVICE)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.findByIdOrNull(assignDto.id) } returns null

                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(assignDto.id)
                }
            }
        }

        Given("Feature에서 Device 제거할 때") {
            When("유효한 요청으로 Device 제거") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.DEVICE)
                val feature = dummyFeature(id = featureId)
                val device = dummyDevice(id = "device-1", feature = feature)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.findByIdOrNull(assignDto.id) } returns device

                Then("성공") {
                    featureService.removeSomethingFromFeature(featureId, assignDto)
                    device.feature shouldBe null
                }
            }

            When("존재하지 않는 Device 제거 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.DEVICE)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.findByIdOrNull(assignDto.id) } returns null

                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.removeSomethingFromFeature(featureId, assignDto)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(assignDto.id)
                }
            }
        }

        Given("Feature 저장할 때") {
            When("유효한 Feature로 저장 요청") {
                val feature = dummyFeature()
                val savedFeature = dummyFeature()

                every { featureRepository.save(feature) } returns savedFeature

                Then("성공") {
                    val response = featureService.saveFeature(feature)
                    response shouldBe savedFeature
                    verify { featureRepository.save(feature) }
                }
            }
        }

        Given("Feature에 CCTV 할당할 때") {
            When("유효한 요청으로 CCTV 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)
                val feature = dummyFeature(id = featureId)
                val slot = slot<String>()

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { deviceRepository.existsByFeature(feature) } returns false
                every { deviceRepository.revokeByFeature(any()) } just runs
                every { featureAssignment.assignFeature(capture(slot), any()) } just runs

                Then("성공") {
                    featureService.assignSomethingToFeature(featureId, assignDto, false)
                    slot.captured shouldBe assignDto.id
                }
            }

            When("이미 할당된 CCTV가 있는 Feature에 강제하지 않고 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { featureAssignment.existsByFeature(feature) } returns true

                Then("ALREADY_FEATURE_ASSIGNED 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }.message shouldBe ErrorCode.DUPLICATE_FEATURE_OTHER_CCTV.getMessage().format(featureId)
                }
            }

            When("이미 다른 Feature에 할당된 CCTV 할당 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { featureAssignment.isAssigned(assignDto.id) } returns true

                Then("ALREADY_ASSIGNED_TARGET 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }.message shouldBe ErrorCode.ALREADY_ASSIGNED_TARGET.getMessage().format(assignDto.id, assignDto.type.description)
                }
            }
        }

        Given("Feature에서 Cctv 제거할 때") {
            When("유효한 요청으로 Cctv 제거") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)

                Then("성공") {
                    featureService.removeSomethingFromFeature(featureId, assignDto)
                    verify(exactly = 1) { featureAssignment.validateRevoke(assignDto.id, featureId) }
                    verify(exactly = 1) { featureAssignment.clearFeatureFromTarget(assignDto.id) }
                }
            }

            When("할당되지 않은 CCTV 제거 시도할 때") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)

                every {
                    featureAssignment.validateRevoke(assignDto.id, featureId)
                } throws CustomException(ErrorCode.CCTV_NOT_ASSIGNED, assignDto.id)

                Then("CCTV_NOT_ASSIGNED 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.removeSomethingFromFeature(featureId, assignDto)
                    }.errorCode shouldBe ErrorCode.CCTV_NOT_ASSIGNED
                }
            }

            When("다른 Feature에 할당된 CCTV 제거 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)

                every {
                    featureAssignment.validateRevoke(assignDto.id, featureId)
                } throws CustomException(ErrorCode.CCTV_MISMATCH)

                Then("CCTV_MISMATCH 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        featureService.removeSomethingFromFeature(featureId, assignDto)
                    }.errorCode shouldBe ErrorCode.CCTV_MISMATCH
                }
            }
        }
    })
