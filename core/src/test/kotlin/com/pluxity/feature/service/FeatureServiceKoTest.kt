
package com.pluxity.feature.service

import com.pluxity.asset.service.AssetValidator
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
        val temperatureHumidityAssignment: FeatureAssignment =
            mockk(relaxed = true) {
                every { type } returns FeatureAssignType.THERMO_HYGROMETER
            }
        val cctvAssignment: FeatureAssignment =
            mockk(relaxed = true) {
                every { type } returns FeatureAssignType.CCTV
            }

        val featureService =
            FeatureService(
                featureRepository,
                facilityService,
                assetValidator,
                FeatureAssignmentRegistry(
                    listOf(
                        temperatureHumidityAssignment,
                        cctvAssignment,
                    ),
                ),
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

                val response = featureService.createFeature(createRequest)
                Then("성공") {
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

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.createFeature(createRequest)
                    }
                Then("DUPLICATE_FEATURE_ID 예외 발생") {
                    exception.message shouldBe ErrorCode.DUPLICATE_FEATURE_ID.getMessage().format(createRequest.id)
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

                val response = featureService.createFeature(createRequest)
                Then("기본값으로 성공") {
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

                val response = featureService.getFeatures(facilityId)
                Then("정상 조회") {
                    response.size shouldBe 2
                    response[0].id shouldBe "feature-1"
                    response[1].id shouldBe "feature-2"
                }
            }

            When("존재하지 않는 facilityId로 조회 요청") {
                val facilityId = 999L

                every { facilityService.findById(facilityId) } throws CustomException(ErrorCode.NOT_FOUND_FACILITY, facilityId)

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.getFeatures(facilityId)
                    }
                Then("NOT_FOUND_FACILITY 예외 발생") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_FACILITY.getMessage().format(facilityId)
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

                val response = featureService.updateFeature(featureId, updateRequest)
                Then("성공") {
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

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.updateFeature(featureId, updateRequest)
                    }
                Then("NOT_FOUND_FEATURE 예외 발생") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_FEATURE.getMessage().format(featureId)
                }
            }
        }

        Given("Feature 삭제를 진행할 때") {
            When("유효한 Feature ID로 삭제 요청") {
                val featureId = "test-feature-id"
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.revokeByFeature(feature) } just runs
                every { cctvAssignment.revokeByFeature(feature) } just runs
                every { featureRepository.delete(feature) } just runs

                featureService.deleteFeature(featureId)
                Then("성공") {
                    verify { temperatureHumidityAssignment.revokeByFeature(feature) }
                    verify { cctvAssignment.revokeByFeature(feature) }
                    verify { featureRepository.delete(feature) }
                }
            }

            When("존재하지 않는 Feature ID로 삭제 요청") {
                val featureId = "non-existing-feature"

                every { featureRepository.findByIdOrNull(featureId) } returns null

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.deleteFeature(featureId)
                    }
                Then("NOT_FOUND_FEATURE 예외 발생") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_FEATURE.getMessage().format(featureId)
                }
            }
        }

        Given("Feature ID로 조회할 때") {
            When("유효한 Feature ID로 조회") {
                val featureId = "test-feature-id"
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature

                val response = featureService.findFeatureById(featureId)
                Then("성공") {
                    response.id shouldBe featureId
                }
            }

            When("존재하지 않는 Feature ID로 조회") {
                val featureId = "non-existing-feature"

                every { featureRepository.findByIdOrNull(featureId) } returns null

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.findFeatureById(featureId)
                    }
                Then("NOT_FOUND_FEATURE 예외 발생") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_FEATURE.getMessage().format(featureId)
                }
            }
        }

        Given("AssetId로 Feature ID 목록 조회할 때") {
            When("유효한 AssetId로 조회") {
                val assetId = 1L
                val features = listOf(dummyFeature(id = "feature-1", assetId = assetId), dummyFeature(id = "feature-2", assetId = assetId))

                every { featureRepository.findByAssetId(assetId) } returns features

                val response = featureService.findFeatureIdsByAssetId(assetId)
                Then("성공") {
                    response[0] shouldBe "feature-1"
                    response[1] shouldBe "feature-2"
                }
            }
        }

        Given("Feature에 Device 할당할 때") {
            When("유효한 요청으로 Device 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.THERMO_HYGROMETER)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.isAssigned(assignDto.id) } returns false
                every { temperatureHumidityAssignment.existsByFeature(feature) } returns false
                every { temperatureHumidityAssignment.revokeByFeature(any()) } just runs
                every { temperatureHumidityAssignment.assignFeature(assignDto.id, feature) } just runs

                featureService.assignSomethingToFeature(featureId, assignDto, false)
                Then("성공") {
                    verify { temperatureHumidityAssignment.assignFeature(assignDto.id, feature) }
                }
            }

            When("이미 할당된 Device가 있는 Feature에 강제 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.THERMO_HYGROMETER)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.revokeByFeature(any()) } just runs
                every { temperatureHumidityAssignment.assignFeature(assignDto.id, feature) } just runs

                featureService.assignSomethingToFeature(featureId, assignDto, true)
                Then("성공") {
                    verify { temperatureHumidityAssignment.assignFeature(assignDto.id, feature) }
                }
            }

            When("이미 할당된 Device가 있는 Feature에 강제하지 않고 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.THERMO_HYGROMETER)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.existsByFeature(feature) } returns true

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }
                Then("ALREADY_FEATURE_ASSIGNED 예외 발생") {
                    exception.message shouldBe ErrorCode.ALREADY_FEATURE_ASSIGNED.getMessage().format(featureId)
                }
            }

            When("이미 다른 Feature에 할당된 Device 할당 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.THERMO_HYGROMETER)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.isAssigned(assignDto.id) } returns true
                every { temperatureHumidityAssignment.existsByFeature(feature) } returns false

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }
                Then("DUPLICATE_DEVICE_OTHER_FEATURE 예외 발생") {
                    exception.message shouldBe
                        ErrorCode.ALREADY_ASSIGNED_TARGET.getMessage().format(assignDto.id, assignDto.type.description)
                }
            }

            When("CCTV가 이미 할당된 Feature에 Device 할당 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.THERMO_HYGROMETER)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { cctvAssignment.existsByFeature(feature) } returns true
                every { temperatureHumidityAssignment.isAssigned(assignDto.id) } returns false

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }
                Then("ALREADY_FEATURE_ASSIGNED 예외 발생") {
                    exception.message shouldBe ErrorCode.ALREADY_FEATURE_ASSIGNED.getMessage().format(featureId)
                }
            }

            When("존재하지 않는 Device 할당 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.THERMO_HYGROMETER)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.isAssigned(assignDto.id) } throws
                    CustomException(ErrorCode.NOT_FOUND_DEVICE, assignDto.id)

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }
                Then("NOT_FOUND_DEVICE 예외 발생") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(assignDto.id)
                }
            }
        }

        Given("Feature에서 Device 제거할 때") {
            When("유효한 요청으로 Device 제거") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.THERMO_HYGROMETER)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.validateRevoke(assignDto.id, featureId) } just runs
                every { temperatureHumidityAssignment.clearFeatureFromTarget(assignDto.id) } just runs

                featureService.removeSomethingFromFeature(featureId, assignDto)
                Then("성공") {
                    verify { temperatureHumidityAssignment.clearFeatureFromTarget(assignDto.id) }
                }
            }

            When("존재하지 않는 Device 제거 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "device-1", type = FeatureAssignType.THERMO_HYGROMETER)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.validateRevoke(assignDto.id, featureId) } throws
                    CustomException(ErrorCode.NOT_FOUND_DEVICE, assignDto.id)

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.removeSomethingFromFeature(featureId, assignDto)
                    }
                Then("NOT_FOUND_DEVICE 예외 발생") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(assignDto.id)
                }
            }
        }

        Given("Feature 저장할 때") {
            When("유효한 Feature로 저장 요청") {
                val feature = dummyFeature()
                val savedFeature = dummyFeature()

                every { featureRepository.save(feature) } returns savedFeature

                val response = featureService.saveFeature(feature)
                Then("성공") {
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
                every { temperatureHumidityAssignment.existsByFeature(feature) } returns false
                every { temperatureHumidityAssignment.revokeByFeature(any()) } just runs
                every { cctvAssignment.assignFeature(capture(slot), any()) } just runs

                featureService.assignSomethingToFeature(featureId, assignDto, false)
                Then("성공") {
                    slot.captured shouldBe assignDto.id
                }
            }

            When("Device가 이미 할당된 Feature에 CCTV 할당 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { temperatureHumidityAssignment.existsByFeature(feature) } returns true

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }
                Then("ALREADY_FEATURE_ASSIGNED 예외 발생") {
                    exception.message shouldBe ErrorCode.ALREADY_FEATURE_ASSIGNED.getMessage().format(featureId)
                }
            }

            When("이미 할당된 CCTV가 있는 Feature에 강제하지 않고 할당") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { cctvAssignment.existsByFeature(feature) } returns true

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }
                Then("ALREADY_FEATURE_ASSIGNED 예외 발생") {
                    exception.message shouldBe ErrorCode.ALREADY_FEATURE_ASSIGNED.getMessage().format(featureId)
                }
            }

            When("이미 다른 Feature에 할당된 CCTV 할당 시도") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { cctvAssignment.isAssigned(assignDto.id) } returns true

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.assignSomethingToFeature(featureId, assignDto, false)
                    }
                Then("ALREADY_ASSIGNED_TARGET 예외 발생") {
                    exception.message shouldBe
                        ErrorCode.ALREADY_ASSIGNED_TARGET.getMessage().format(assignDto.id, assignDto.type.description)
                }
            }
        }

        Given("Feature에서 Cctv 제거할 때") {
            When("유효한 요청으로 Cctv 제거") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)
                val feature = dummyFeature(id = featureId)
                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { cctvAssignment.validateRevoke(assignDto.id, featureId) } just runs
                every { cctvAssignment.clearFeatureFromTarget(assignDto.id) } just runs

                Then("성공") {
                    featureService.removeSomethingFromFeature(featureId, assignDto)
                    verify(exactly = 1) { cctvAssignment.validateRevoke(assignDto.id, featureId) }
                    verify(exactly = 1) { cctvAssignment.clearFeatureFromTarget(assignDto.id) }
                }
            }

            When("유효하지 않은 요청으로 Cctv 제거") {
                val featureId = "test-feature-id"
                val assignDto = FeatureAssignDto(id = "cctv-1", type = FeatureAssignType.CCTV)
                val feature = dummyFeature(id = featureId)

                every { featureRepository.findByIdOrNull(featureId) } returns feature
                every { cctvAssignment.validateRevoke(assignDto.id, featureId) } throws
                    CustomException(ErrorCode.CCTV_MISMATCH)

                val exception =
                    shouldThrowExactly<CustomException> {
                        featureService.removeSomethingFromFeature(featureId, assignDto)
                    }
                Then("CCTV_MISMATCH 예외 발생") {
                    exception.message shouldBe ErrorCode.CCTV_MISMATCH.getMessage()
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
