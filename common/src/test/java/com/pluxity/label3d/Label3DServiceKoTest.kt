package com.pluxity.label3d

import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.feature.entity.Spatial
import com.pluxity.feature.entity.dummyFeature
import com.pluxity.feature.service.FeatureService
import com.pluxity.global.utils.SortUtils
import com.pluxity.label3d.entity.dummyLabel3d
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class Label3DServiceKoTest :
    BehaviorSpec({
        isolationMode = IsolationMode.InstancePerLeaf

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
                        position =
                            Spatial
                                .builder()
                                .x(1.0)
                                .y(2.0)
                                .z(3.0)
                                .build(),
                        rotation =
                            Spatial
                                .builder()
                                .x(0.0)
                                .y(90.0)
                                .z(0.0)
                                .build(),
                        scale =
                            Spatial
                                .builder()
                                .x(1.0)
                                .y(1.0)
                                .z(1.0)
                                .build(),
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

                    result shouldBe
                        Label3DResponse(
                            id = createRequest.id,
                            displayText = createRequest.displayText!!,
                            floorId = createRequest.floorId,
                            position = createRequest.position,
                            rotation = createRequest.rotation,
                            scale = createRequest.scale,
                        )
                    verify(exactly = 1) { facilityService.findById(createRequest.facilityId) }
                    verify(exactly = 1) { featureService.saveFeature(any()) }
                    verify(exactly = 1) { label3DRepository.save(any()) }
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
                    verify(exactly = 1) { label3DRepository.findById(id) }
                }
            }

            When("없는 ID로 조회 요청") {
                val id = "non-existent-id"

                every { label3DRepository.findByIdOrNull(id) } returns null

                Then("EntityNotFoundException 발생") {
                    shouldThrowExactly<EntityNotFoundException> {
                        label3DService.getLabel3DById(id)
                    }.message shouldBe "Label3D not found with id: $id"
                }
            }
        }

        Given("Label3D 전체 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                val sort = mockk<Sort>()
                val id = "test-id"
                val feature = dummyFeature(id = id, floorId = "floor-1")
                val label3D = dummyLabel3d(id = id, feature = feature, displayText = "Test Label")

                mockkStatic(SortUtils::class)
                every { SortUtils.getOrderByCreatedAtDesc() } returns sort
                every { label3DRepository.findAll(sort) } returns listOf(label3D)

                Then("정상 조회") {
                    val result = label3DService.getAllLabel3Ds()

                    result.size shouldBe 1
                    result[0].id shouldBe id
                    result[0].displayText shouldBe "Test Label"
                    result[0].floorId shouldBe "floor-1"
                    verify(exactly = 1) { label3DRepository.findAll(sort) }
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
                    verify(exactly = 1) { label3DRepository.findAllByFacilityId(facilityId) }
                }
            }
        }

        Given("Label3D 수정을 진행할 때") {
            When("유효한 ID와 요청으로 수정 요청") {
                val id = "test-id"
                val updateRequest =
                    Label3DUpdateRequest(
                        position =
                            Spatial
                                .builder()
                                .x(2.0)
                                .y(3.0)
                                .z(4.0)
                                .build(),
                        rotation =
                            Spatial
                                .builder()
                                .x(0.0)
                                .y(180.0)
                                .z(0.0)
                                .build(),
                        scale =
                            Spatial
                                .builder()
                                .x(2.0)
                                .y(2.0)
                                .z(2.0)
                                .build(),
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

                Then("EntityNotFoundException 발생") {
                    shouldThrowExactly<EntityNotFoundException> {
                        label3DService.updateLabel3D(id, updateRequest)
                    }.message shouldBe "Label3D not found with id: $id"
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
                every { label3DRepository.delete(label3D) } just runs

                Then("성공") {
                    label3DService.deleteLabel3D(id)

                    verify(exactly = 1) { label3DRepository.findById(id) }
                    verify(exactly = 1) { featureService.deleteFeature(any()) }
                    verify(exactly = 1) { label3DRepository.delete(label3D) }
                }
            }

            When("없는 ID로 삭제 요청") {
                val id = "non-existent-id"

                every { label3DRepository.findByIdOrNull(id) } returns null

                Then("EntityNotFoundException 발생") {
                    shouldThrowExactly<EntityNotFoundException> {
                        label3DService.deleteLabel3D(id)
                    }.message shouldBe "Label3D not found with id: $id"
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
                    verify(exactly = 1) { label3DRepository.findById(id) }
                }
            }

            When("없는 ID로 findLabel3DById 호출") {
                val id = "non-existent-id"

                every { label3DRepository.findByIdOrNull(id) } returns null

                Then("EntityNotFoundException 발생") {
                    shouldThrowExactly<EntityNotFoundException> {
                        label3DService.findLabel3DById(id)
                    }.message shouldBe "Label3D not found with id: $id"
                }
            }
        }
    })
