package com.pluxity.cctv

import com.ninjasquad.springmockk.MockkBean
import com.pluxity.GsApplication
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.RoleGlobalPermissionType
import com.pluxity.user.repository.RoleGlobalPolicyRepository
import com.pluxity.user.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest(classes = [GsApplication::class])
@ActiveProfiles("test")
class CctvRepositoryPermissionTest : BehaviorSpec() {
    @MockkBean lateinit var userService: UserService

    @MockkBean lateinit var roleGlobalPolicyRepository: RoleGlobalPolicyRepository

    @Autowired lateinit var cctvRepository: CctvRepository

    init {
        extension(SpringExtension)

        beforeTest {
            initAuthUser(userService, roleGlobalPolicyRepository)
        }

        afterTest {
            SecurityContextHolder.clearContext()
            clearMocks(userService, roleGlobalPolicyRepository)
        }

        Given("CCTV 조회 권한 체크") {
            When("글로벌 정책이 READ이고 id 권한이 있으면 조회된다") {
                Then("CCTV가 조회된다") {
                    setUserWithPermission(userService, PermissionLevel.READ)
                    cctvRepository.save(Cctv("c1", "name", "url"))
                    val result = cctvRepository.findByIdOrNullCustom("c1")
                    result?.id shouldBe "c1"
                }
            }

            When("글로벌 정책이 READ지만 id 권한이 없으면 거부") {
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    setUserWithPermission(userService, PermissionLevel.READ, resourceId = "c2")
                    cctvRepository.save(Cctv("c1", "name", "url"))
                    val exception =
                        shouldThrow<CustomException> {
                            cctvRepository.findByIdOrNullCustom("c1")
                        }
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                }
            }

            When("READ_ALL 권한이면 조회된다") {
                Then("CCTV가 조회된다") {
                    every {
                        roleGlobalPolicyRepository.existsByRoleIdInAndResourceTypeAndPermissionTypeIn(
                            any(),
                            ResourceType.CCTV,
                            match { it.contains(RoleGlobalPermissionType.READ_ALL) },
                        )
                    } returns true
                    cctvRepository.save(Cctv("c1", "name", "url"))
                    val result = cctvRepository.findByIdOrNullCustom("c1")
                    result?.id shouldBe "c1"
                }
            }

            When("READ_ALL 권한이 없고 id 권한도 없으면 조회는 거부") {
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    setUserWithPermission(userService, PermissionLevel.READ, resourceId = "c2")
                    every {
                        roleGlobalPolicyRepository.existsByRoleIdInAndResourceTypeAndPermissionTypeIn(
                            any(),
                            ResourceType.CCTV,
                            any(),
                        )
                    } returns false
                    cctvRepository.save(Cctv("c1", "name", "url"))
                    val exception =
                        shouldThrow<CustomException> {
                            cctvRepository.findByIdOrNullCustom("c1")
                        }
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                }
            }
        }
    }
}
