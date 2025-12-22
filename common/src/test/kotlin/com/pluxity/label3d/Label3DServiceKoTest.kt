package com.pluxity.label3d

import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.feature.entity.Spatial
import com.pluxity.feature.service.FeatureService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.SortUtils
import com.pluxity.label3d.entity.dummyLabel3d
import entity.dummyFeature
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class Label3DServiceKoTest :
    BehaviorSpec({
        val label3DRepository: Label3DRepository = mockk()
        val featureService: FeatureService = mockk()
        val facilityService: FacilityService = mockk()

        val label3DService = Label3DService(label3DRepository, featureService, facilityService)

        Given("Label3D 생성을 진행할 때") {
            When("유효한 요청으로 Label3D 생성 요청") {
                val createRequest =
                    Label3DCreateRequest(
                        id = "test-id",
                        displayText = "Test Label",
                        facilityId = 1L,
                        floorId = "floor-1",
                        position = Spatial(1.0, 2.0, 3.0),
                        rotation = Spatial(0.0, 90.0, 0.0),
                        scale = Spatial(1.0, 1.0, 1.0),
                    )

                val facility = mockk<Facility>()
                val feature =
                    dummyFeature(
                        id = createRequest.id,
                        floorId = createRequest.floorId,
                        position = createRequest.position,
                        rotation = createRequest.rotation,
                        scale = createRequest.scale,
                    )
                val label3D =
                    dummyLabel3d(
                        id = createRequest.id,
                        feature = feature,
                        displayText = createRequest.displayText,
                    )

                every { facilityService.findById(createRequest.facilityId) } returns facility
                every { featureService.saveFeature(any()) } returns feature
                every { label3DRepository.save(any()) } returns label3D

                Then("성공") {
                    val result = label3DService.createLabel3D(createRequest)

                    result shouldBe createRequest.id
                }
            }
        }

        Given("Label3D 단건 조회를 진행할 때") {
            When("유효한 ID로 조회 요청") {
                val id = "test-id"
                val feature = dummyFeature(id = id, floorId = "floor-1")
                val label3D = dummyLabel3d(id = id, feature = feature, displayText = "Test Label")

                every { label3DRepository.findByIdOrNull(id) } returns label3D

                Then("정상 조회") {
                    val result = label3DService.getLabel3DById(id)

                    result.id shouldBe id
                    result.displayText shouldBe "Test Label"
                    result.floorId shouldBe "floor-1"
                }
            }

            When("없는 ID로 조회 요청") {
                val id = "non-existent-id"

                every { label3DRepository.findByIdOrNull(id) } returns null

                Then("CustomException(NOT_FOUND_LABEL_3D) 발생") {
                    shouldThrowExactly<CustomException> {
                        label3DService.getLabel3DById(id)
                    }.message shouldBe ErrorCode.NOT_FOUND_LABEL_3D.getMessage().format(id)
                }
            }
        }

        Given("Label3D 전체 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                val id = "test-id"
                val feature = dummyFeature(id = id, floorId = "floor-1")
                val label3D = dummyLabel3d(id = id, feature = feature, displayText = "Test Label")

                mockkStatic(SortUtils::class)
                every { label3DRepository.findAll(any<Sort>()) } returns listOf(label3D)

                Then("정상 조회") {
                    val result = label3DService.getAllLabel3Ds()

                    result.size shouldBe 1
                    result[0].id shouldBe id
                    result[0].displayText shouldBe "Test Label"
                    result[0].floorId shouldBe "floor-1"
                }
            }
        }

        Given("Facility별 Label3D 목록 조회를 진행할 때") {
            When("유효한 facilityId로 조회 요청") {
                val facilityId = 1L
                val id = "test-id"
                val feature = dummyFeature(id = id, floorId = "floor-1")
                val label3D = dummyLabel3d(id = id, feature = feature, displayText = "Test Label")

                every { label3DRepository.findAllByFacilityId(facilityId) } returns listOf(label3D)

                Then("정상 조회") {
                    val result = label3DService.getLabel3DsByFacilityId(facilityId)

                    result.size shouldBe 1
                    result[0].id shouldBe id
                    result[0].displayText shouldBe "Test Label"
                    result[0].floorId shouldBe "floor-1"
                }
            }
        }

        Given("Label3D 수정을 진행할 때") {
            When("유효한 ID와 요청으로 수정 요청") {
                val id = "test-id"
                val updateRequest =
                    Label3DUpdateRequest(
                        position = Spatial(2.0, 3.0, 4.0),
                        rotation = Spatial(0.0, 180.0, 0.0),
                        scale = Spatial(2.0, 2.0, 2.0),
                    )
                val feature = dummyFeature(id = id, floorId = "floor-1")
                val label3D = Label3D(feature = feature, displayText = "Test Label")

                every { label3DRepository.findByIdOrNull(id) } returns label3D

                Then("성공") {
                    label3DService.updateLabel3D(id, updateRequest)
                    feature.position shouldBe updateRequest.position
                    feature.rotation shouldBe updateRequest.rotation
                    feature.scale shouldBe updateRequest.scale
                }
            }

            When("없는 ID로 수정 요청") {
                val id = "non-existent-id"
                val updateRequest = Label3DUpdateRequest(null, null, null)

                every { label3DRepository.findByIdOrNull(id) } returns null

                Then("CustomException(NOT_FOUND_LABEL_3D) 발생") {
                    shouldThrowExactly<CustomException> {
                        label3DService.updateLabel3D(id, updateRequest)
                    }.message shouldBe ErrorCode.NOT_FOUND_LABEL_3D.getMessage().format(id)
                }
            }
        }

        Given("Label3D 삭제를 진행할 때") {
            When("유효한 ID로 삭제 요청") {
                val id = "test-id"
                val feature = dummyFeature(id = id, floorId = "floor-1")
                val label3D = Label3D(feature = feature, displayText = "Test Label")

                every { label3DRepository.findByIdOrNull(id) } returns label3D
                every { featureService.deleteFeature(any()) } just runs
                every { label3DRepository.deleteById(id) } just runs

                Then("성공") {
                    label3DService.deleteLabel3D(id)

                    verify(exactly = 1) { featureService.deleteFeature(any()) }
                    verify(exactly = 1) { label3DRepository.deleteById(id) }
                }
            }

            When("없는 ID로 삭제 요청") {
                val id = "non-existent-id"

                every { label3DRepository.findByIdOrNull(id) } returns null

                Then("CustomException(NOT_FOUND_LABEL_3D) 발생") {
                    shouldThrowExactly<CustomException> {
                        label3DService.deleteLabel3D(id)
                    }.message shouldBe ErrorCode.NOT_FOUND_LABEL_3D.getMessage().format(id)
                }
            }
        }

        Given("Label3D 조회 헬퍼 메서드를 진행할 때") {
            When("유효한 ID로 findLabel3DById 호출") {
                val id = "test-id"
                val feature = dummyFeature(id = id, floorId = "floor-1")
                val label3D = Label3D(feature = feature, displayText = "Test Label")

                every { label3DRepository.findByIdOrNull(id) } returns label3D

                Then("정상 반환") {
                    val result = label3DService.findLabel3DById(id)

                    result shouldBe label3D
                }
            }

            When("없는 ID로 findLabel3DById 호출") {
                val id = "non-existent-id"

                every { label3DRepository.findByIdOrNull(id) } returns null

                Then("CustomException(NOT_FOUND_LABEL_3D) 발생") {
                    shouldThrowExactly<CustomException> {
                        label3DService.findLabel3DById(id)
                    }.message shouldBe ErrorCode.NOT_FOUND_LABEL_3D.getMessage().format(id)
                }
            }
        }
    })
