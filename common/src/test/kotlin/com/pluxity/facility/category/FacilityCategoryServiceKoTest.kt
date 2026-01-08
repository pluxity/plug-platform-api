package com.pluxity.facility.category

import com.pluxity.category.dto.CategoryDepthResponse
import com.pluxity.facility.Facility
import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import facility.dummyFacilityCategory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class FacilityCategoryServiceKoTest :
    BehaviorSpec({

        val facilityCategoryRepository = mockk<FacilityCategoryRepository>()
        val facilityService = FacilityCategoryService(facilityCategoryRepository)

        Given("시설 카테고리 생성 요청을 할 때") {
            When("부모 카테고리 없이 생성 요청하면") {
                val request =
                    FacilityCategoryCreateRequest(
                        name = "test category",
                        parentId = null,
                    )

                val savedCategory = dummyFacilityCategory(name = request.name)

                every { facilityCategoryRepository.findByNameAndParentId(request.name, null) } returns null
                every { facilityCategoryRepository.save(any()) } returns savedCategory

                val result = facilityService.create(request)

                Then("루트 카테고리가 생성되고 ID를 반환한다") {
                    result shouldBe 1L
                    verify(exactly = 1) { facilityCategoryRepository.findByNameAndParentId(request.name, null) }
                    verify(exactly = 1) { facilityCategoryRepository.save(any()) }
                }
            }

            When("부모 카테고리와 함께 생성 요청하면") {
                val parent = dummyFacilityCategory(id = 1L, name = "부모")
                val request = FacilityCategoryCreateRequest(name = "자식", parentId = 1L)
                val savedCategory =
                    dummyFacilityCategory(id = 2L, name = "자식").apply {
                        this.parent = parent
                    }

                every { facilityCategoryRepository.findByNameAndParentId("자식", 1L) } returns null
                every { facilityCategoryRepository.findByIdOrNull(1L) } returns parent
                every { facilityCategoryRepository.save(any()) } returns savedCategory

                val result = facilityService.create(request)

                Then("자식 카테고리가 생성되고 부모와 연결된다") {
                    result shouldBe 2L
                    verify(exactly = 1) { facilityCategoryRepository.findByIdOrNull(1L) }
                }
            }

            When("같은 부모 하에 중복된 이름으로 생성 요청하면") {
                val request = FacilityCategoryCreateRequest(name = "중복", parentId = 1L)
                val existingCategory = dummyFacilityCategory(id = 99L, name = "중복")

                every { facilityCategoryRepository.findByNameAndParentId("중복", 1L) } returns existingCategory

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.create(request)
                    }

                Then("INVALID_REFERENCE 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.INVALID_REFERENCE
                    exception.message shouldBe ErrorCode.INVALID_REFERENCE.getMessage().format("중복")
                    verify(exactly = 0) { facilityCategoryRepository.save(any()) }
                }
            }

            When("부모 ID가 유효하지 않으면") {
                val request = FacilityCategoryCreateRequest(name = "카테고리", parentId = 999L)

                every { facilityCategoryRepository.findByNameAndParentId(any(), any()) } returns null
                every { facilityCategoryRepository.findByIdOrNull(999L) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.create(request)
                    }

                Then("NOT_FOUND_CATEGORY 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_CATEGORY
                }
            }
        }

        Given("전체 시설 카테고리 조회 요청을 할 때") {
            When("카테고리가 하나도 없으면") {
                every { facilityCategoryRepository.findByParentIsNull(any<Sort>()) } returns emptyList()

                val result = facilityService.findAll()

                Then("빈 리스트를 반환한다") {
                    result shouldBe emptyList()
                }
            }

            When("루트 카테고리만 존재하면") {
                val root1 = dummyFacilityCategory(id = 1L, name = "카테고리1")
                val root2 = dummyFacilityCategory(id = 2L, name = "카테고리2")

                every { facilityCategoryRepository.findByParentIsNull(any<Sort>()) } returns listOf(root1, root2)

                val result = facilityService.findAll()

                Then("모든 루트 카테고리를 반환한다") {
                    result.size shouldBe 2
                }
            }

            When("루트와 자식 카테고리가 모두 존재하면") {
                val parent = dummyFacilityCategory(id = 1L, name = "부모")
                val child = dummyFacilityCategory(id = 2L, name = "자식")
                child.parent = parent
                parent.children.add(child)

                every { facilityCategoryRepository.findByParentIsNull(any<Sort>()) } returns listOf(parent)

                val result = facilityService.findAll()

                Then("루트 카테고리만 반환하고 자식은 children에 포함된다") {
                    result.size shouldBe 1
                }
            }
        }

        Given("시설 카테고리 수정 요청을 할 때") {
            When("존재하지 않는 ID로 수정 요청하면") {
                val request = FacilityCategoryUpdateRequest(name = "수정", parentId = null)

                every { facilityCategoryRepository.findByIdOrNull(999L) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.update(999L, request)
                    }

                Then("NOT_FOUND_CATEGORY 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_CATEGORY
                }
            }

            When("이름만 변경하면") {
                val category = dummyFacilityCategory(id = 1L, name = "원래이름")
                val request = FacilityCategoryUpdateRequest(name = "새이름", parentId = null)

                every { facilityCategoryRepository.findByIdOrNull(1L) } returns category
                every { facilityCategoryRepository.findByNameAndParentId("새이름", null) } returns null

                facilityService.update(1L, request)

                Then("카테고리 이름이 수정된다") {
                    category.name shouldBe "새이름"
                    verify(exactly = 1) { facilityCategoryRepository.findByNameAndParentId("새이름", null) }
                }
            }

            When("이름과 부모가 기존과 동일하면") {
                val category = dummyFacilityCategory(id = 1L, name = "동일")
                val request = FacilityCategoryUpdateRequest(name = "동일", parentId = null)

                every { facilityCategoryRepository.findByIdOrNull(1L) } returns category

                facilityService.update(1L, request)

                Then("중복 검증을 생략한다") {
                    verify(exactly = 0) { facilityCategoryRepository.findByNameAndParentId(any(), any()) }
                }
            }

            When("같은 부모 하에 중복된 이름으로 수정 요청하면") {
                val category = dummyFacilityCategory(id = 1L, name = "원래")
                val duplicateCategory = dummyFacilityCategory(id = 2L, name = "중복")
                val request = FacilityCategoryUpdateRequest(name = "중복", parentId = null)

                every { facilityCategoryRepository.findByIdOrNull(1L) } returns category
                every { facilityCategoryRepository.findByNameAndParentId("중복", null) } returns duplicateCategory

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.update(1L, request)
                    }

                Then("INVALID_REFERENCE 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.INVALID_REFERENCE
                }
            }
        }

        Given("시설 카테고리 삭제 요청을 할 때") {
            When("존재하지 않는 ID로 삭제 요청하면") {
                every { facilityCategoryRepository.findByIdOrNull(999L) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.delete(999L)
                    }

                Then("NOT_FOUND_CATEGORY 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_CATEGORY
                }
            }

            When("자식 카테고리가 없고 시설도 없으면") {
                val category = dummyFacilityCategory(id = 1L, name = "test category")

                every { facilityCategoryRepository.findByIdOrNull(1L) } returns category
                every { facilityCategoryRepository.delete(category) } just runs

                facilityService.delete(1L)

                Then("정상적으로 삭제된다") {
                    verify(exactly = 1) { facilityCategoryRepository.delete(category) }
                }
            }

            When("자식 카테고리가 있으면") {
                val parent = dummyFacilityCategory(id = 1L, name = "parent category")
                val child = dummyFacilityCategory(id = 2L, name = "child category")
                parent.children.add(child)

                every { facilityCategoryRepository.findByIdOrNull(1L) } returns parent

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.delete(1L)
                    }

                Then("CATEGORY_HAS_CHILDREN 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.CATEGORY_HAS_CHILDREN
                    verify(exactly = 0) { facilityCategoryRepository.delete(any()) }
                }
            }

            When("시설이 연결되어 있으면") {
                val category = dummyFacilityCategory(id = 1L, name = "test category")
                val facility = mockk<Facility>()
                category.facilities.add(facility)

                every { facilityCategoryRepository.findByIdOrNull(1L) } returns category

                val exception =
                    shouldThrow<CustomException> {
                        facilityService.delete(1L)
                    }

                Then("FACILITY_CATEGORY_HAS_FACILITY 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.FACILITY_CATEGORY_HAS_FACILITY
                    verify(exactly = 0) { facilityCategoryRepository.delete(any()) }
                }
            }
        }

        Given("카테고리 깊이 조회 요청을 할 때") {
            When("정상적으로 호출하면") {
                val result = facilityService.getCategoryDepth()

                Then("FacilityCategory의 maxDepth를 반환한다") {
                    result shouldBe CategoryDepthResponse(FacilityCategory().maxDepth)
                }
            }
        }
    })
