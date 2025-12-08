package com.pluxity.onboarding

import com.pluxity.device.dto.DeviceCategoryRequest
import com.pluxity.device.dto.DeviceCreateRequest
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.device.service.DeviceService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.onboarding.service.OnboardingDeviceService
import com.pluxity.permission.PermissionGroupService
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionRequest
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.UserCreateRequest
import com.pluxity.user.entity.RoleType
import com.pluxity.user.service.RoleService
import com.pluxity.user.service.UserService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional


/**
 * 온보딩 과제2: 장비 제어 권한 체크 서비스 테스트
 */
@SpringBootTest
@Transactional
class OnboardingStep6BusinessLogic @Autowired constructor(
    private val deviceControlledService: OnboardingDeviceService,
    private val deviceService: DeviceService,
    private val categoryService: DeviceCategoryService,
    private val permissionGroupService: PermissionGroupService,
    private val roleService: RoleService,
    private val userService: UserService
    ) {
    private lateinit var sensorId: String
    private lateinit var cctvId: String
    private var sensorCategoryId: Long = 0L
    private var cctvCategoryId: Long = 0L
    private var cctvPermission: Long = 0L
    private var adminRoleId: Long = 0L
    private var userRoleId: Long = 0L

    @BeforeEach
    fun setUp() {

        // 1. category 생성(sensor, cctv)
        sensorCategoryId = categoryService.create(DeviceCategoryRequest(
            name = "센서",
            parentId = null,
            thumbnailFileId = null
        ))

        cctvCategoryId = categoryService.create(DeviceCategoryRequest(
            name = "CCTV",
            parentId = null,
            thumbnailFileId = null
        ))


        sensorId = deviceService.save(DeviceCreateRequest(
            id = "sensor-001",
            name = "센서",
            categoryId = sensorCategoryId,
            companyType = DeviceCompanyType.DAWONDNS,
            deviceType = DeviceType.TEMP_HUM
        ))

        cctvId = deviceService.save(DeviceCreateRequest(
            id = "cctv-001",
            name = "CCTV",
            categoryId = cctvCategoryId,
            companyType = DeviceCompanyType.DAWONDNS,
            deviceType = DeviceType.TEMP_HUM
        ))

        cctvPermission = permissionGroupService.create(PermissionGroupCreateRequest(
            name = "cctvPermission",
            description = "cctv 권한 그룹",
            permissions = listOf(
                PermissionRequest(
                    ResourceType.DEVICE_CATEGORY.name,
                    listOf(cctvCategoryId.toString())
                )
            )
        ))

        adminRoleId = roleService.save(RoleCreateRequest(
            name = RoleType.ADMIN.name,
            description = "test-role-desc",
            permissionGroupIds = listOf()
        ))

        userRoleId = roleService.save(RoleCreateRequest(
            name = RoleType.USER.name,
            description = "test-role-desc",
            permissionGroupIds = listOf(cctvPermission)
        ))
    }

    @Test
    @DisplayName("ADMIN 권한을 가진 유저는 모든 장비 제어 가능")
    fun userWithAdminRole_controlAnyDevice_success() {
        // given
        val admin = userService.save(UserCreateRequest(
            username = "admin-user",
            password = "password123",
            name = "관리자",
            roleIds = listOf(adminRoleId)
        ))

        // when & then - 센서 제어 가능
        Assertions.assertThatCode {
            deviceControlledService.controlDevice(
                userId = admin.id,
                deviceId = sensorId
            )
        }.doesNotThrowAnyException()

        // CCTV도 제어 가능
        Assertions.assertThatCode {
            deviceControlledService.controlDevice(
                userId = admin.id,
                deviceId = cctvId
            )
        }.doesNotThrowAnyException()
    }

    @Test
    @DisplayName("특정 카테고리 권한을 가진 유저는 해당 카테고리 장비만 제어 가능")
    fun userWithCategoryPermission_controlAuthorizedDevice_success() {
        // given: CCTV 권한만 있는 유저
        val cctvUser = userService.save(UserCreateRequest(
            username = "cctv-user",
            password = "password123",
            name = "CCTV 관리자",
            roleIds = listOf(userRoleId)  // cctvPermission 포함
        ))

        // when & then: CCTV 제어 성공
        Assertions.assertThatCode {
            deviceControlledService.controlDevice(
                userId = cctvUser.id,
                deviceId = cctvId
            )
        }.doesNotThrowAnyException()
    }

    @Test
    @DisplayName("권한 없는 유저가 장비 제어 시 PERMISSION_DENIED 예외 발생")
    fun userWithoutPermission_controlDevice_throwsPermissionDeniedException() {
        // given: CCTV 권한만 있는 유저
        val cctvUser = userService.save(UserCreateRequest(
            username = "cctv-only-user",
            password = "password123",
            name = "CCTV 전용",
            roleIds = listOf(userRoleId)
        ))

        // when & then: 센서 제어 시 예외
        val exception = assertThrows<CustomException> {
            deviceControlledService.controlDevice(
                userId = cctvUser.id,
                deviceId = sensorId  // 권한 없는 센서
            )
        }

        // 예외 상세 검증
        Assertions.assertThat(exception.errorCode).isEqualTo(ErrorCode.PERMISSION_DENIED)
        Assertions.assertThat(exception.message).contains("권한")
    }



}