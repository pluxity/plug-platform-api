package com.pluxity.facility

import com.ninjasquad.springmockk.MockkBean
import com.pluxity.CoreApplication
import com.pluxity.building.Building
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.user.service.UserService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.mockk.clearMocks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import util.initAuthUser
import util.setUserWithPermission

@Transactional
@SpringBootTest(classes = [CoreApplication::class])
@ActiveProfiles("test")
class FacilityRepositoryPermissionTest : BehaviorSpec() {
    @MockkBean lateinit var userService: UserService

    @Autowired lateinit var facilityRepository: FacilityRepository

    init {
        extension(SpringExtension)

        beforeTest {
            initAuthUser(userService)
        }

        afterTest {
            SecurityContextHolder.clearContext()
            clearMocks(userService)
        }

        Given("시설 조회 권한 체크") {
            When("리소스 id에 READ 권한이 있으면") {
                Then("시설 목록이 조회된다") {
                    val building = Building("테스트 건물", "설명")
                    val saved = facilityRepository.save(building)
                    setUserWithPermission(userService, ResourceType.FACILITY, PermissionLevel.READ, saved.id!!)
                    val result = facilityRepository.findAllByOrderByCreatedAtDesc()
                    result.shouldNotBeEmpty()
                }
            }

            When("Domain 권한이 READ 권한이면") {
                Then("시설 목록이 조회된다") {
                    val building = Building("테스트 건물", "설명")
                    val saved = facilityRepository.save(building)
                    setUserWithPermission(userService, ResourceType.FACILITY, PermissionLevel.READ, saved.id!!)
                    val result = facilityRepository.findAllByOrderByCreatedAtDesc()
                    result.shouldNotBeEmpty()
                }
            }
        }
    }
}
