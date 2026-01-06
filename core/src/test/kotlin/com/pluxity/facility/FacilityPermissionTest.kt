package com.pluxity.facility

import base.entity.withId
import com.ninjasquad.springmockk.MockkBean
import com.pluxity.CoreApplication
import com.pluxity.building.Building
import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.facility.dto.FacilityUpdateRequest
import com.pluxity.facility.history.FacilityHistoryService
import com.pluxity.facility.path.FacilityPathService
import com.pluxity.facility.strategy.FloorService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.DomainPermission
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourcePermission
import com.pluxity.permission.ResourceType
import com.pluxity.user.service.UserResourcePermissionService
import com.pluxity.user.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import util.initAuthUser
import util.setUserWithPermission
import util.setUserWithPermissions

@SpringBootTest(classes = [CoreApplication::class])
@ActiveProfiles("test")
class FacilityPermissionTest : BehaviorSpec() {
    @MockkBean lateinit var userService: UserService

    @MockkBean lateinit var facilityRepository: FacilityRepository

    @MockkBean lateinit var fileService: FileService

    @MockkBean lateinit var facilityHistoryService: FacilityHistoryService

    @MockkBean lateinit var facilityPathService: FacilityPathService

    @MockkBean lateinit var floorService: FloorService

    @MockkBean lateinit var userResourcePermissionService: UserResourcePermissionService

    @Autowired lateinit var facilityService: FacilityService

    init {
        extension(SpringExtension)

        beforeTest {
            initAuthUser(userService)
        }

        afterTest {
            SecurityContextHolder.clearContext()
            clearMocks(
                userService,
                facilityRepository,
                fileService,
                facilityHistoryService,
                facilityPathService,
                floorService,
                userResourcePermissionService,
            )
        }

        Given("시설 생성/수정/삭제 권한 체크") {

            When("글로벌 정책이 없으면 생성은 거부") {
                val building = Building("테스트 건물", "설명")
                val request = FacilityCreateRequest(name = "테스트 건물", code = "CODE1", description = "설명")
                val exception =
                    shouldThrow<CustomException> {
                        facilityService.save(building, request)
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { facilityRepository.save(any()) }
                    verify(exactly = 0) { userResourcePermissionService.create(any(), any(), any()) }
                }
            }

            When("글로벌 정책이 있으면 생성 후 소유권을 등록") {
                setUserWithPermissions(
                    userService,
                    resourcePermissions = emptyList(),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.FACILITY.name,
                                level = PermissionLevel.WRITE,
                            ),
                        ),
                )
                val building = Building("테스트 건물", "설명").withId(1L)
                val request = FacilityCreateRequest("테스트 건물", "CODE1", "설명", null, null, null, null, null)
                every { facilityRepository.existsByCode(any()) } returns false
                every { facilityRepository.save(any()) } returns building
                every { userResourcePermissionService.create(any(), any(), any()) } just runs
                val result = facilityService.save(building, request)
                Then("등록 호출이 수행된다") {
                    result.id shouldBe 1L
                    verify(exactly = 1) { userResourcePermissionService.create(10L, ResourceType.FACILITY, any()) }
                }
            }

            When("권한이 없으면 수정은 거부") {
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        facilityService.putUpdate(1L, FacilityUpdateRequest(name = "수정된 이름"))
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { facilityRepository.findById(any()) }
                }
            }

            When("수정 요청 시 권한이 READ인 경우") {
                setUserWithPermission(userService, ResourceType.FACILITY, PermissionLevel.READ, 1L)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        facilityService.putUpdate(1L, FacilityUpdateRequest(name = "수정된 이름"))
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { facilityRepository.findById(any()) }
                }
            }

            When("수정 요청 시 권한이 WRITE인 경우") {
                setUserWithPermission(userService, ResourceType.FACILITY, PermissionLevel.WRITE, 1L)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val building = Building("테스트 건물", "설명").withId(1L)
                every { facilityRepository.findById(1L) } returns java.util.Optional.of(building)
                facilityService.putUpdate(1L, FacilityUpdateRequest(name = "수정된 이름"))
                Then("정상 수정된다") {
                    building.name shouldBe "수정된 이름"
                }
            }

            When("수정 요청 시 권한이 ADMIN인 경우") {
                setUserWithPermission(userService, ResourceType.FACILITY, PermissionLevel.ADMIN, 1L)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val building = Building("테스트 건물", "설명").withId(1L)
                every { facilityRepository.findById(1L) } returns java.util.Optional.of(building)
                facilityService.putUpdate(1L, FacilityUpdateRequest(name = "수정된 이름"))
                Then("정상 수정된다") {
                    building.name shouldBe "수정된 이름"
                }
            }

            When("소유주면 수정이 정상 동작") {
                setUserWithPermissions(
                    userService,
                    resourcePermissions = emptyList(),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.FACILITY.name,
                                level = PermissionLevel.WRITE,
                            ),
                        ),
                )
                val building = Building("테스트 건물", "설명").withId(1L)
                every { facilityRepository.findById(1L) } returns java.util.Optional.of(building)
                every { userResourcePermissionService.exists(10L, ResourceType.FACILITY, "1") } returns true
                facilityService.putUpdate(1L, FacilityUpdateRequest(name = "수정된 이름"))
                Then("시설이 업데이트된다") {
                    building.name shouldBe "수정된 이름"
                }
            }

            When("글로벌 WRITE 권한이면 수정이 허용된다") {
                val building = Building("테스트 건물", "설명").withId(1L)
                every { facilityRepository.findById(1L) } returns java.util.Optional.of(building)
                setUserWithPermissions(
                    userService,
                    resourcePermissions =
                        listOf(
                            ResourcePermission(
                                resourceName = ResourceType.FACILITY.name,
                                resourceId = "1",
                                level = PermissionLevel.WRITE,
                            ),
                        ),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.FACILITY.name,
                                level = PermissionLevel.WRITE,
                            ),
                        ),
                )
                facilityService.putUpdate(1L, FacilityUpdateRequest(name = "수정된 이름"))
                Then("정상 수정된다") {
                    building.name shouldBe "수정된 이름"
                }
            }

            When("소유자가 아니면 삭제는 거부") {
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        facilityService.deleteFacility(1L)
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { facilityRepository.findById(any()) }
                }
            }

            When("삭제 요청 시 권한이 READ인 경우") {
                setUserWithPermission(userService, ResourceType.FACILITY, PermissionLevel.READ, 1L)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        facilityService.deleteFacility(1L)
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { facilityRepository.findById(any()) }
                }
            }

            When("삭제 요청 시 권한이 WRITE인 경우") {
                setUserWithPermission(userService, ResourceType.FACILITY, PermissionLevel.WRITE, 1L)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        facilityService.deleteFacility(1L)
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { facilityRepository.findById(any()) }
                }
            }

            When("삭제 요청 시 권한이 ADMIN인 경우") {
                setUserWithPermission(userService, ResourceType.FACILITY, PermissionLevel.ADMIN, 1L)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                every { userResourcePermissionService.delete(any(), any()) } just runs
                val building = Building("테스트 건물", "설명").withId(1L)
                every { facilityRepository.findById(1L) } returns java.util.Optional.of(building)
                every { facilityRepository.delete(any()) } just runs
                facilityService.deleteFacility(1L)
                Then("정상 삭제된다") {
                    verify(exactly = 1) { facilityRepository.delete(building) }
                    verify(exactly = 1) { userResourcePermissionService.delete(ResourceType.FACILITY, any()) }
                }
            }

            When("글로벌 ADMIN 권한이면 삭제가 허용된다") {
                val building = Building("테스트 건물", "설명").withId(1L)
                every { facilityRepository.findById(1L) } returns java.util.Optional.of(building)
                every { facilityRepository.delete(any()) } just runs
                every { userResourcePermissionService.delete(any(), any()) } just runs
                setUserWithPermissions(
                    userService,
                    resourcePermissions =
                        listOf(
                            ResourcePermission(
                                resourceName = ResourceType.FACILITY.name,
                                resourceId = "1",
                                level = PermissionLevel.ADMIN,
                            ),
                        ),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.FACILITY.name,
                                level = PermissionLevel.ADMIN,
                            ),
                        ),
                )
                facilityService.deleteFacility(1L)
                Then("정상 삭제된다") {
                    verify(exactly = 1) { facilityRepository.delete(building) }
                    verify(exactly = 1) { userResourcePermissionService.delete(ResourceType.FACILITY, any()) }
                }
            }

            When("소유주면 삭제 후 소유권이 해제") {
                setUserWithPermissions(
                    userService,
                    resourcePermissions = emptyList(),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.FACILITY.name,
                                level = PermissionLevel.ADMIN,
                            ),
                        ),
                )
                val building = Building("테스트 건물", "설명").withId(1L)
                every { facilityRepository.findById(1L) } returns java.util.Optional.of(building)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns true
                every { userResourcePermissionService.delete(any(), any()) } just runs
                every { facilityRepository.delete(any()) } just runs
                facilityService.deleteFacility(1L)
                Then("삭제 및 delete가 수행된다") {
                    verify(exactly = 1) { facilityRepository.delete(building) }
                    verify(exactly = 1) { userResourcePermissionService.delete(ResourceType.FACILITY, any()) }
                }
            }
        }
    }
}
