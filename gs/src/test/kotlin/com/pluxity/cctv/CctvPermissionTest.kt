package com.pluxity.cctv

import com.ninjasquad.springmockk.MockkBean
import com.pluxity.GsApplication
import com.pluxity.cctv.dto.CctvCreateRequest
import com.pluxity.cctv.dto.CctvUpdateRequest
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.cctv.repository.DeviceCctvRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.RoleGlobalPermissionType
import com.pluxity.user.repository.RoleGlobalPolicyRepository
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

@SpringBootTest(classes = [GsApplication::class])
@ActiveProfiles("test")
class CctvPermissionTest : BehaviorSpec() {
    @MockkBean lateinit var userService: UserService

    @MockkBean lateinit var cctvRepository: CctvRepository

    @MockkBean lateinit var deviceCctvRepository: DeviceCctvRepository

    @MockkBean lateinit var roleGlobalPolicyRepository: RoleGlobalPolicyRepository

    @MockkBean lateinit var userResourcePermissionService: UserResourcePermissionService

    @Autowired lateinit var cctvService: CctvService

    init {
        extension(SpringExtension)

        beforeTest {
            initAuthUser(userService, roleGlobalPolicyRepository)
        }

        afterTest {
            SecurityContextHolder.clearContext()
            clearMocks(
                userService,
                cctvRepository,
                deviceCctvRepository,
                roleGlobalPolicyRepository,
                userResourcePermissionService,
            )
        }

        Given("CCTV 생성/수정/삭제 권한 체크") {

            When("글로벌 정책이 없으면 생성은 거부") {
                every {
                    roleGlobalPolicyRepository.existsByRoleIdInAndResourceType(
                        listOf(1L),
                        ResourceType.CCTV,
                    )
                } returns false

                val exception =
                    shouldThrow<CustomException> {
                        cctvService.create(CctvCreateRequest("c1", "name", "url"))
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { cctvRepository.save(any()) }
                    verify(exactly = 0) { userResourcePermissionService.create(10L, ResourceType.CCTV, "c1") }
                }
            }

            When("글로벌 정책이 있으면 생성 후 소유권을 등록") {
                every {
                    roleGlobalPolicyRepository.existsByRoleIdInAndResourceType(
                        listOf(1L),
                        ResourceType.CCTV,
                    )
                } returns true
                every { cctvRepository.save(any()) } returns Cctv("c1", "name", "url")
                every { userResourcePermissionService.create(10L, ResourceType.CCTV, "c1") } just runs
                val result = cctvService.create(CctvCreateRequest("c1", "name", "url"))
                Then("등록 호출이 수행된다") {
                    result shouldBe "c1"
                    verify(exactly = 1) { userResourcePermissionService.create(10L, ResourceType.CCTV, "c1") }
                }
            }

            When("권한이 없으면 수정은 거부") {
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        cctvService.update("c1", CctvUpdateRequest("new-name", "new-url"))
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { cctvRepository.findById(any()) }
                }
            }

            When("수정 권한이 READ인 경우") {
                setUserWithPermission(userService, PermissionLevel.READ)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        cctvService.update("c1", CctvUpdateRequest("new-name", "new-url"))
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { cctvRepository.findById(any()) }
                }
            }

            When("수정 권한이 WRITE인 경우") {
                setUserWithPermission(userService, PermissionLevel.WRITE)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val cctv = Cctv("c1", "name", "url")
                every { cctvRepository.findByIdOrNullCustom("c1") } returns cctv
                cctvService.update("c1", CctvUpdateRequest("new-name", "new-url"))
                Then("정상 수정된다") {
                    cctv.name shouldBe "new-name"
                    cctv.url shouldBe "new-url"
                }
            }

            When("수정 권한이 ADMIN인 경우") {
                setUserWithPermission(userService, PermissionLevel.ADMIN)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val cctv = Cctv("c1", "name", "url")
                every { cctvRepository.findByIdOrNullCustom("c1") } returns cctv
                cctvService.update("c1", CctvUpdateRequest("new-name", "new-url"))
                Then("정상 수정된다") {
                    cctv.name shouldBe "new-name"
                    cctv.url shouldBe "new-url"
                }
            }

            When("소유주면 수정이 정상 동작") {
                val cctv = Cctv("c1", "name", "url")
                every { cctvRepository.findByIdOrNullCustom("c1") } returns cctv
                every { userResourcePermissionService.exists(10L, ResourceType.CCTV, "c1") } returns true
                cctvService.update("c1", CctvUpdateRequest("new-name", "new-url"))
                Then("CCTV가 업데이트된다") {
                    cctv.name shouldBe "new-name"
                    cctv.url shouldBe "new-url"
                }
            }

            When("글로벌 WRITE_ALL 권한이면 수정이 허용된다") {
                val cctv = Cctv("c1", "name", "url")
                every { cctvRepository.findByIdOrNullCustom("c1") } returns cctv
                every {
                    roleGlobalPolicyRepository.existsByRoleIdInAndResourceTypeAndPermissionTypeIn(
                        listOf(1L),
                        ResourceType.CCTV,
                        listOf(RoleGlobalPermissionType.WRITE_ALL, RoleGlobalPermissionType.ADMIN),
                    )
                } returns true
                cctvService.update("c1", CctvUpdateRequest("new-name", "new-url"))
                Then("정상 수정된다") {
                    cctv.name shouldBe "new-name"
                    cctv.url shouldBe "new-url"
                }
            }

            When("권한이 없으면 삭제는 거부") {
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        cctvService.delete("c1")
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { cctvRepository.findById(any()) }
                }
            }

            When("삭제 권한이 READ인 경우") {
                setUserWithPermission(userService, PermissionLevel.READ)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        cctvService.delete("c1")
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { cctvRepository.findById(any()) }
                }
            }

            When("삭제 권한이 WRITE인 경우") {
                setUserWithPermission(userService, PermissionLevel.WRITE)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        cctvService.delete("c1")
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { cctvRepository.findById(any()) }
                }
            }

            When("삭제 권한이 ADMIN인 경우") {
                setUserWithPermission(userService, PermissionLevel.ADMIN)
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                every { userResourcePermissionService.delete(10L, ResourceType.CCTV, "c1") } just runs
                val cctv = Cctv("c1", "name", "url")
                every { cctvRepository.findByIdOrNullCustom("c1") } returns cctv
                every { deviceCctvRepository.deleteByCctvIdIn(any()) } just runs
                every { cctvRepository.deleteById(any()) } just runs
                cctvService.delete("c1")
                Then("정상 삭제된다") {
                    verify(exactly = 1) { deviceCctvRepository.deleteByCctvIdIn(listOf("c1")) }
                    verify(exactly = 1) { cctvRepository.deleteById("c1") }
                    verify(exactly = 1) { userResourcePermissionService.delete(10L, ResourceType.CCTV, "c1") }
                }
            }

            When("글로벌 ADMIN 권한이면 삭제가 허용된다") {
                val cctv = Cctv("c1", "name", "url")
                every { cctvRepository.findByIdOrNullCustom("c1") } returns cctv
                every { deviceCctvRepository.deleteByCctvIdIn(any()) } just runs
                every { cctvRepository.deleteById(any()) } just runs
                every { userResourcePermissionService.delete(10L, ResourceType.CCTV, "c1") } just runs
                every {
                    roleGlobalPolicyRepository.existsByRoleIdInAndResourceTypeAndPermissionTypeIn(
                        listOf(1L),
                        ResourceType.CCTV,
                        listOf(RoleGlobalPermissionType.ADMIN),
                    )
                } returns true
                cctvService.delete("c1")
                Then("정상 삭제된다") {
                    verify(exactly = 1) { deviceCctvRepository.deleteByCctvIdIn(listOf("c1")) }
                    verify(exactly = 1) { cctvRepository.deleteById("c1") }
                    verify(exactly = 1) { userResourcePermissionService.delete(10L, ResourceType.CCTV, "c1") }
                }
            }

            When("소유주면 삭제 후 소유권이 해제") {
                val cctv = Cctv("c1", "name", "url")
                every { cctvRepository.findByIdOrNullCustom("c1") } returns cctv
                every { userResourcePermissionService.exists(10L, ResourceType.CCTV, "c1") } returns true
                every { userResourcePermissionService.delete(10L, ResourceType.CCTV, "c1") } just runs
                every { deviceCctvRepository.deleteByCctvIdIn(any()) } just runs
                every { cctvRepository.deleteById(any()) } just runs
                cctvService.delete("c1")
                Then("삭제 및 delete가 수행된다") {
                    verify(exactly = 1) { deviceCctvRepository.deleteByCctvIdIn(listOf("c1")) }
                    verify(exactly = 1) { cctvRepository.deleteById("c1") }
                    verify(exactly = 1) { userResourcePermissionService.delete(10L, ResourceType.CCTV, "c1") }
                }
            }
        }
    }
}
