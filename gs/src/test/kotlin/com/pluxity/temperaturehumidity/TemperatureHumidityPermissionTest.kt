package com.pluxity.temperaturehumidity

import com.ninjasquad.springmockk.MockkBean
import com.pluxity.GsApplication
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.DomainPermission
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourcePermission
import com.pluxity.permission.ResourceType
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityCreateRequest
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityUpdateRequest
import com.pluxity.temperaturehumidity.entity.TemperatureHumidity
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
import com.pluxity.temperaturehumidity.service.TemperatureHumidityService
import com.pluxity.user.service.UserResourcePermissionService
import com.pluxity.user.service.UserService
import com.pluxity.util.initAuthUser
import com.pluxity.util.setUserWithPermission
import com.pluxity.util.setUserWithPermissions
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
class TemperatureHumidityPermissionTest : BehaviorSpec() {
    @MockkBean lateinit var userService: UserService

    @MockkBean lateinit var temperatureHumidityRepository: TemperatureHumidityRepository

    @MockkBean lateinit var userResourcePermissionService: UserResourcePermissionService

    @Autowired lateinit var temperatureHumidityService: TemperatureHumidityService

    init {
        extension(SpringExtension)

        beforeTest {
            initAuthUser(userService)
        }

        afterTest {
            SecurityContextHolder.clearContext()
            clearMocks(
                userService,
                temperatureHumidityRepository,
                userResourcePermissionService,
            )
        }

        Given("온습도계 생성/수정/삭제 권한 체크") {

            When("글로벌 정책이 없으면 생성은 거부") {
                val exception =
                    shouldThrow<CustomException> {
                        temperatureHumidityService.save(TemperatureHumidityCreateRequest("th1", "name"))
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { temperatureHumidityRepository.save(any()) }
                    verify(exactly = 0) { userResourcePermissionService.create(10L, ResourceType.TEMPERATURE_HUMIDITY, "th1") }
                }
            }

            When("글로벌 정책이 있으면 생성 후 소유권을 등록") {
                setUserWithPermissions(
                    userService,
                    resourcePermissions = emptyList(),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.TEMPERATURE_HUMIDITY.name,
                                level = PermissionLevel.WRITE,
                            ),
                        ),
                )
                every { temperatureHumidityRepository.save(any()) } returns TemperatureHumidity("th1", "name")
                every { userResourcePermissionService.create(10L, ResourceType.TEMPERATURE_HUMIDITY, "th1") } just runs
                val result = temperatureHumidityService.save(TemperatureHumidityCreateRequest("th1", "name"))
                Then("등록 호출이 수행된다") {
                    result shouldBe "th1"
                    verify(exactly = 1) { userResourcePermissionService.create(10L, ResourceType.TEMPERATURE_HUMIDITY, "th1") }
                }
            }

            When("권한이 없으면 수정은 거부") {
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        temperatureHumidityService.putUpdate("th1", TemperatureHumidityUpdateRequest("new-name"))
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { temperatureHumidityRepository.findByIdOrNullCustom(any()) }
                }
            }

            When("수정 요청 시 권한이 READ인 경우") {
                setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.READ, "th1")
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        temperatureHumidityService.putUpdate("th1", TemperatureHumidityUpdateRequest("new-name"))
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { temperatureHumidityRepository.findByIdOrNullCustom(any()) }
                }
            }

            When("수정 요청 시 권한이 WRITE인 경우") {
                setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.WRITE, "th1")
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val th = TemperatureHumidity("th1", "name")
                every { temperatureHumidityRepository.findByIdOrNullCustom("th1") } returns th
                temperatureHumidityService.putUpdate("th1", TemperatureHumidityUpdateRequest("new-name"))
                Then("정상 수정된다") {
                    th.name shouldBe "new-name"
                }
            }

            When("수정 요청 시 권한이 ADMIN인 경우") {
                setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.ADMIN, "th1")
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val th = TemperatureHumidity("th1", "name")
                every { temperatureHumidityRepository.findByIdOrNullCustom("th1") } returns th
                temperatureHumidityService.putUpdate("th1", TemperatureHumidityUpdateRequest("new-name"))
                Then("정상 수정된다") {
                    th.name shouldBe "new-name"
                }
            }

            When("소유주면 수정이 정상 동작") {
                setUserWithPermissions(
                    userService,
                    resourcePermissions = emptyList(),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.TEMPERATURE_HUMIDITY.name,
                                level = PermissionLevel.WRITE,
                            ),
                        ),
                )
                val th = TemperatureHumidity("th1", "name")
                every { temperatureHumidityRepository.findByIdOrNullCustom("th1") } returns th
                every { userResourcePermissionService.exists(10L, ResourceType.TEMPERATURE_HUMIDITY, "th1") } returns true
                temperatureHumidityService.putUpdate("th1", TemperatureHumidityUpdateRequest("new-name"))
                Then("온습도계가 업데이트된다") {
                    th.name shouldBe "new-name"
                }
            }

            When("글로벌 WRITE 권한이면 수정이 허용된다") {
                val th = TemperatureHumidity("th1", "name")
                every { temperatureHumidityRepository.findByIdOrNullCustom("th1") } returns th
                setUserWithPermissions(
                    userService,
                    resourcePermissions =
                        listOf(
                            ResourcePermission(
                                resourceName = ResourceType.TEMPERATURE_HUMIDITY.name,
                                resourceId = "th1",
                                level = PermissionLevel.WRITE,
                            ),
                        ),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.TEMPERATURE_HUMIDITY.name,
                                level = PermissionLevel.WRITE,
                            ),
                        ),
                )
                temperatureHumidityService.putUpdate("th1", TemperatureHumidityUpdateRequest("new-name"))
                Then("정상 수정된다") {
                    th.name shouldBe "new-name"
                }
            }

            When("소유자가 아니면 삭제는 거부") {
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        temperatureHumidityService.delete("th1")
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { temperatureHumidityRepository.findByIdOrNullCustom(any()) }
                }
            }

            When("삭제 요청 시 권한이 READ인 경우") {
                setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.READ, "th1")
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        temperatureHumidityService.delete("th1")
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { temperatureHumidityRepository.findByIdOrNullCustom(any()) }
                }
            }

            When("삭제 요청 시 권한이 WRITE인 경우") {
                setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.WRITE, "th1")
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                val exception =
                    shouldThrow<CustomException> {
                        temperatureHumidityService.delete("th1")
                    }
                Then("PERMISSION_DENIED 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.PERMISSION_DENIED
                    verify(exactly = 0) { temperatureHumidityRepository.findByIdOrNullCustom(any()) }
                }
            }

            When("삭제 요청 시 권한이 ADMIN인 경우") {
                setUserWithPermission(userService, ResourceType.TEMPERATURE_HUMIDITY, PermissionLevel.ADMIN, "th1")
                every { userResourcePermissionService.exists(any(), any(), any()) } returns false
                every { userResourcePermissionService.delete(ResourceType.TEMPERATURE_HUMIDITY, "th1") } just runs
                val th = TemperatureHumidity("th1", "name")
                every { temperatureHumidityRepository.findByIdOrNullCustom("th1") } returns th
                every { temperatureHumidityRepository.deleteById(any()) } just runs
                temperatureHumidityService.delete("th1")
                Then("정상 삭제된다") {
                    verify(exactly = 1) { temperatureHumidityRepository.deleteById("th1") }
                    verify(exactly = 1) { userResourcePermissionService.delete(ResourceType.TEMPERATURE_HUMIDITY, "th1") }
                }
            }

            When("글로벌 ADMIN 권한이면 삭제가 허용된다") {
                val th = TemperatureHumidity("th1", "name")
                every { temperatureHumidityRepository.findByIdOrNullCustom("th1") } returns th
                every { temperatureHumidityRepository.deleteById(any()) } just runs
                every { userResourcePermissionService.delete(ResourceType.TEMPERATURE_HUMIDITY, "th1") } just runs
                setUserWithPermissions(
                    userService,
                    resourcePermissions =
                        listOf(
                            ResourcePermission(
                                resourceName = ResourceType.TEMPERATURE_HUMIDITY.name,
                                resourceId = "th1",
                                level = PermissionLevel.ADMIN,
                            ),
                        ),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.TEMPERATURE_HUMIDITY.name,
                                level = PermissionLevel.ADMIN,
                            ),
                        ),
                )
                temperatureHumidityService.delete("th1")
                Then("정상 삭제된다") {
                    verify(exactly = 1) { temperatureHumidityRepository.deleteById("th1") }
                    verify(exactly = 1) { userResourcePermissionService.delete(ResourceType.TEMPERATURE_HUMIDITY, "th1") }
                }
            }

            When("소유주면 삭제 후 소유권이 해제") {
                setUserWithPermissions(
                    userService,
                    resourcePermissions = emptyList(),
                    domainPermissions =
                        listOf(
                            DomainPermission(
                                resourceName = ResourceType.TEMPERATURE_HUMIDITY.name,
                                level = PermissionLevel.ADMIN,
                            ),
                        ),
                )
                val th = TemperatureHumidity("th1", "name")
                every { temperatureHumidityRepository.findByIdOrNullCustom("th1") } returns th
                every { userResourcePermissionService.exists(10L, ResourceType.TEMPERATURE_HUMIDITY, "th1") } returns true
                every { userResourcePermissionService.delete(ResourceType.TEMPERATURE_HUMIDITY, "th1") } just runs
                every { temperatureHumidityRepository.deleteById(any()) } just runs
                temperatureHumidityService.delete("th1")
                Then("삭제 및 delete가 수행된다") {
                    verify(exactly = 1) { temperatureHumidityRepository.deleteById("th1") }
                    verify(exactly = 1) { userResourcePermissionService.delete(ResourceType.TEMPERATURE_HUMIDITY, "th1") }
                }
            }
        }
    }
}
