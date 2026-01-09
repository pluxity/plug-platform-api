package com.pluxity.temperaturehumidity

import com.ninjasquad.springmockk.MockkBean
import com.pluxity.GsApplication
import com.pluxity.cctv.MediaMtxService
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.temperaturehumidity.entity.TemperatureHumidity
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
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
class TemperatureHumidityRepositoryPermissionTest : BehaviorSpec() {
    @MockkBean lateinit var userService: UserService

    @MockkBean(relaxed = true)
    lateinit var mediaMtxService: MediaMtxService

    @MockkBean(relaxed = true)
    lateinit var cctvRepository: CctvRepository

    @Autowired lateinit var temperatureHumidityRepository: TemperatureHumidityRepository

    init {
        extension(SpringExtension)

        beforeTest {
            initAuthUser(userService)
        }

        afterTest {
            SecurityContextHolder.clearContext()
            clearMocks(userService)
        }

        Given("온습도계 조회 권한 체크") {
            When("리소스 id에 READ 권한이 있으면") {
                Then("온습도계가 조회된다") {
                    setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.READ, "th1")
                    temperatureHumidityRepository.save(TemperatureHumidity("th1", "name"))
                    val result = temperatureHumidityRepository.findByIdOrNullCustom("th1")
                    result?.id shouldBe "th1"
                }
            }

            When("리소스 id에 READ 권한이 없으면") {
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.READ, "th2")
                    temperatureHumidityRepository.save(TemperatureHumidity("th1", "name"))
                    val exception =
                        shouldThrow<CustomException> {
                            temperatureHumidityRepository.findByIdOrNullCustom("th1")
                        }
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                }
            }

            When("Domain 권한이 READ 권한이면") {
                Then("온습도계가 조회된다") {
                    setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.READ, "th1")
                    temperatureHumidityRepository.save(TemperatureHumidity("th1", "name"))
                    val result = temperatureHumidityRepository.findByIdOrNullCustom("th1")
                    result?.id shouldBe "th1"
                }
            }
        }
    }
}
