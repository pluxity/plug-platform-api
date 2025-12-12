package com.pluxity.onboarding

import com.pluxity.climate.ClimateData
import com.pluxity.climate.ClimateDataRepository
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityRepository
import com.pluxity.feature.entity.Feature
import com.pluxity.feature.repository.FeatureRepository
import com.pluxity.permission.Permission
import com.pluxity.permission.PermissionGroup
import com.pluxity.permission.PermissionGroupRepository
import com.pluxity.permission.PermissionRepository
import com.pluxity.permission.ResourceType
import com.pluxity.station.Station
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RolePermission
import com.pluxity.user.entity.User
import com.pluxity.user.repository.RolePermissionRepository
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
import com.pluxity.user.repository.UserRoleRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.TimeUnit

@SpringBootTest
class OnboardingStep9Scenario
    @Autowired
    constructor(
        private val job: OnboardingJob,
        private val climateDataRepository: ClimateDataRepository,
        private val alertRepository: AlertRepository,
        private val roleRepository: RoleRepository,
        private val permissionGroupRepository: PermissionGroupRepository,
        private val permissionRepository: PermissionRepository,
        private val rolePermissionRepository: RolePermissionRepository,
        private val userRoleRepository: UserRoleRepository,
        private val deviceRepository: DeviceRepository,
        private val facilityRepository: FacilityRepository,
        private val userRepository: UserRepository,
        private val featureRepository: FeatureRepository,
    ) {
        @MockitoBean lateinit var collector: OnboardingCollector

        lateinit var facility: Facility
        lateinit var device: Device
        lateinit var user: User

        @BeforeEach
        fun setUp() {
            climateDataRepository.deleteAll()
            alertRepository.deleteAll()
            userRoleRepository.deleteAll()
            rolePermissionRepository.deleteAll()
            roleRepository.deleteAll()
            userRepository.deleteAll()
            permissionRepository.deleteAll()
            permissionGroupRepository.deleteAll()
            deviceRepository.deleteAll()
            featureRepository.deleteAll()
            facilityRepository.deleteAll()

            facility = facilityRepository.save(Station("test-station"))

            val permission =
                permissionRepository.save(
                    Permission(
                        resourceName = ResourceType.FACILITY.name, // "FACILITY"
                        resourceId = facility.id.toString(),
                    ),
                )

            val permissionGroup =
                permissionGroupRepository.save(
                    PermissionGroup(
                        name = "Station Access Group",
                        description = "Can access specific station",
                    ),
                )
            permissionGroup.addPermission(permission)
            permissionRepository.save(permission)

            val role =
                roleRepository.save(
                    Role(
                        name = "STATION_MANAGER",
                        description = "Manages Stations",
                    ),
                )

            val rolePermission =
                rolePermissionRepository.save(
                    RolePermission(
                        role = role,
                        permissionGroup = permissionGroup,
                    ),
                )

            role.addRolePermission(rolePermission)

            user =
                userRepository.save(
                    User(
                        username = "manager",
                        password = "password",
                        name = "Manager Kim",
                        code = "M001",
                        department = "Facility Dept",
                    ),
                )

            user.addRole(role)
            userRepository.save(user)

            val feature = featureRepository.save(Feature(id = "feature-1", facility = facility))
            device =
                deviceRepository.save(
                    Device(
                        id = "DEV-001",
                        name = "Sensor 1",
                        feature = feature,
                        deviceType = DeviceType.TEMP_HUM,
                        companyType = DeviceCompanyType.DAWONDNS,
                    ),
                )
        }

        // Case 1: 정상 알림
        @Test
        @DisplayName("온도 80도 이상이면 데이터 저장 및 알림이 생성된다")
        fun shouldSaveDataAndCreateAlert_WhenTemperatureIsOver80() {
            // given
            val badData = ClimateData(deviceId = device.id, temperature = 85.0)

            runBlocking {
                given(collector.collectData()).willReturn(listOf(badData))
            }

            // when
            job.trigger()

            // then
            await().atMost(2, TimeUnit.SECONDS).untilAsserted {
                val savedData = climateDataRepository.findAll()
                assertThat(savedData).hasSize(1)
                assertThat(savedData[0].temperature).isEqualTo(85.0)

                val alerts = alertRepository.findAll()
                assertThat(alerts).hasSize(1)
                assertThat(alerts[0].device?.id).isEqualTo(device.id)
                assertThat(alerts[0].user?.id).isEqualTo(user.id)
            }
        }

        @DisplayName("온도 80도 미만이면 데이터만 저장되고 알림은 생성되지 않는다")
        @Test
        fun shouldOnlySaveData_WhenTemperatureIsUnder80() {
            // given
            val normalData = ClimateData(deviceId = device.id, temperature = 79.9)

            runBlocking {
                given(collector.collectData()).willReturn(listOf(normalData))
            }

            // when
            job.trigger()

            // then
            await().atMost(2, TimeUnit.SECONDS).untilAsserted {
                assertThat(climateDataRepository.findAll()).hasSize(1)
                assertThat(alertRepository.findAll()).isEmpty()
            }
        }
    }
