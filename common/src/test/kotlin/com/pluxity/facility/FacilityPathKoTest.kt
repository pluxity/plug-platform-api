package com.pluxity.facility

import com.pluxity.facility.path.FacilityPath
import com.pluxity.facility.path.FacilityPathRepository
import com.pluxity.facility.path.FacilityPathService
import com.pluxity.facility.path.PathType
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

class FacilityPathKoTest :
    BehaviorSpec({

        val facilityPathRepository = mockk<FacilityPathRepository>()
        val service = FacilityPathService(facilityPathRepository)

        val invalidId = -1L
        val validId = 1L

        Given("조회 요청이 주어지고") {
            When("유효하지 않은 id로 조회시") {
                every { facilityPathRepository.findByIdOrNull(invalidId) } returns null
                Then("NOT_FOUND_FACILITY_PATH 예외가 발생한다.") {
                    val exception =
                        shouldThrowExactly<CustomException> {
                            service.findById(invalidId)
                        }
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY_PATH
                    exception.message shouldBe ErrorCode.NOT_FOUND_FACILITY_PATH.getMessage().format(invalidId)
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(invalidId) }
                }
            }

            When("유효한 id로 조회시") {
                val facilityPath = mockk<FacilityPath> { every { id } returns validId }
                every { facilityPathRepository.findByIdOrNull(validId) } returns facilityPath
                val result = service.findById(validId)
                Then("엔티티를 반환한다.") {
                    result.id shouldBe validId
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(validId) }
                }
            }
        }

        Given("수정 요청이 주어지고") {
            When("유효하지 않은 id로 수정 시") {
                every { facilityPathRepository.findByIdOrNull(invalidId) } returns null
                Then("NOT_FOUND_FACILITY_PATH 예외가 발생한다.") {
                    val exception =
                        shouldThrowExactly<CustomException> {
                            service.update(invalidId, null, null, null)
                        }
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY_PATH
                    exception.message shouldBe ErrorCode.NOT_FOUND_FACILITY_PATH.getMessage().format(invalidId)
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(invalidId) }
                }
            }

            When("name이 공백일 경우") {
                val updateName = ""
                val testFacilityPath =
                    FacilityPath(
                        name = "test-name",
                        pathType = PathType.WAY,
                        path = "test-path",
                    )

                every { facilityPathRepository.findByIdOrNull(invalidId) } returns testFacilityPath
                service.update(invalidId, updateName, null, null)
                Then(" 수정 되지 않는다.") {
                    testFacilityPath.name shouldBe "test-name"
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(invalidId) }
                }
            }

            When("name이 공백이 아닐경우") {
                val updatedName = "update-name"
                val testFacilityPath =
                    FacilityPath(
                        name = "test-name",
                        pathType = PathType.WAY,
                        path = "test-path",
                    )

                every { facilityPathRepository.findByIdOrNull(validId) } returns testFacilityPath
                service.update(validId, updatedName, null, null)
                Then("수정에 성공한다.") {
                    testFacilityPath.name shouldBe updatedName
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(validId) }
                }
            }

            When("pathType이 잘못된 경우") {
                val invalidPathType = "invalid-path-type"
                val testFacilityPath =
                    FacilityPath(
                        name = "test-name",
                        pathType = PathType.WAY,
                        path = "test-path",
                    )

                every { facilityPathRepository.findByIdOrNull(validId) } returns testFacilityPath

                val exception =
                    shouldThrowExactly<CustomException> {
                        service.update(validId, null, invalidPathType, null)
                    }

                Then("NOT_FOUND_PATH_TYPE 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_PATH_TYPE
                    exception.message shouldBe ErrorCode.NOT_FOUND_PATH_TYPE.getMessage().format(invalidPathType)
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(validId) }
                }
            }

            When("여러 필드를 동시에 수정할 경우") {
                val updateName = "update-name"
                val updatePath = "update-path"
                val updatePathType = PathType.PATROL
                val testFacilityPath =
                    FacilityPath(
                        name = "test-name",
                        pathType = PathType.WAY,
                        path = "test-path",
                    )

                every { facilityPathRepository.findByIdOrNull(validId) } returns testFacilityPath
                service.update(validId, updateName, updatePathType.name, updatePath)
                Then("수정에 성공한다.") {
                    testFacilityPath.name shouldBe updateName
                    testFacilityPath.path shouldBe updatePath
                    testFacilityPath.pathType shouldBe updatePathType
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(validId) }
                }
            }
        }

        Given("삭제 요청이 주어지고") {
            When("유효하지 않은 id로 삭제 시") {
                every { facilityPathRepository.findByIdOrNull(invalidId) } returns null
                val exception =
                    shouldThrowExactly<CustomException> {
                        service.delete(invalidId)
                    }
                Then("NOT_FOUND_FACILITY_PATH 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY_PATH
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(invalidId) }
                    verify(exactly = 0) { facilityPathRepository.delete(any()) }
                }
            }

            When("유효한 id로 삭제 시") {
                val facilityPath = mockk<FacilityPath>()
                every { facilityPathRepository.findByIdOrNull(validId) } returns facilityPath
                every { facilityPathRepository.delete(facilityPath) } just runs

                Then("정상적으로 삭제된다.") {
                    service.delete(validId)
                    verify(exactly = 1) { facilityPathRepository.delete(facilityPath) }
                    verify(exactly = 1) { facilityPathRepository.findByIdOrNull(validId) }
                }
            }
        }
    })
