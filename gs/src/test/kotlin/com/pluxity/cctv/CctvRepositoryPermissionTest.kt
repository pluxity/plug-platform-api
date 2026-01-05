package com.pluxity.cctv

import com.ninjasquad.springmockk.MockkBean
import com.pluxity.GsApplication
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.user.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import util.initAuthUser
import util.setUserWithPermission

@Transactional
@SpringBootTest(classes = [GsApplication::class])
@ActiveProfiles("test")
class CctvRepositoryPermissionTest : BehaviorSpec() {
    @MockkBean lateinit var userService: UserService

    @Autowired lateinit var cctvRepository: CctvRepository

    init {
        extension(SpringExtension)

        beforeTest {
            initAuthUser(userService)
        }

        afterTest {
            SecurityContextHolder.clearContext()
            clearMocks(userService)
        }

        Given("CCTV 조회 권한 체크") {
            When("리소스 id에 READ 권한이 있으면") {
                Then("CCTV가 조회된다") {
                    setUserWithPermission(userService, ResourceType.CCTV, PermissionLevel.READ, "c1")
                    cctvRepository.save(Cctv("c1", "name", "url"))
                    val result = cctvRepository.findByIdOrNullCustom("c1")
                    result?.id shouldBe "c1"
                }
            }

            When("리소스 id에 READ 권한이 없으면") {
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    setUserWithPermission(userService, ResourceType.CCTV, PermissionLevel.READ, "c2")
                    cctvRepository.save(Cctv("c1", "name", "url"))
                    val exception =
                        shouldThrow<CustomException> {
                            cctvRepository.findByIdOrNullCustom("c1")
                        }
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                }
            }

            When("Domain 권한이 READ 권한이면") {
                Then("CCTV가 조회된다") {
                    setUserWithPermission(userService, ResourceType.CCTV, PermissionLevel.READ, "c1")
                    cctvRepository.save(Cctv("c1", "name", "url"))
                    val result = cctvRepository.findByIdOrNullCustom("c1")
                    result?.id shouldBe "c1"
                }
            }
        }
    }
}
