package com.pluxity.user

import com.pluxity.config.MockBeansConfig
import com.pluxity.permission.PermissionGroupRepository
import com.pluxity.permission.PermissionGroupService
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionGroupUpdateRequest
import com.pluxity.permission.dto.PermissionRequest
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleUpdateRequest
import com.pluxity.user.dto.UserCreateRequest
import com.pluxity.user.dto.UserUpdateRequest
import com.pluxity.user.repository.RolePermissionRepository
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
import com.pluxity.user.repository.UserRoleRepository
import com.pluxity.user.service.RoleService
import com.pluxity.user.service.UserService
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import kotlin.properties.Delegates

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class UserRolePermissionGroupTest {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var roleService: RoleService

    @Autowired
    private lateinit var permissionGroupService: PermissionGroupService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var permissionGroupRepository: PermissionGroupRepository

    @Autowired
    private lateinit var userRoleRepository: UserRoleRepository

    @Autowired
    private lateinit var rolePermissionRepository: RolePermissionRepository

    @Autowired
    private lateinit var em: EntityManager

    private var adminUserId by Delegates.notNull<Long>()
    private var operatorUserId: Long by Delegates.notNull()
    private var adminRoleId: Long by Delegates.notNull()
    private var operatorRoleId: Long by Delegates.notNull()
    private var viewerRoleId: Long by Delegates.notNull()
    private var mainFacilityGroupId: Long by Delegates.notNull()
    private var subFacilityGroupId: Long by Delegates.notNull()
    private var deviceCategoryGroupId: Long by Delegates.notNull()

    @BeforeEach
    fun setUp() {
        // === GIVEN: FACILITY와 DEVICE_CATEGORY만 사용하는 복잡한 초기 상태 설정 ===

        // 1. Permission Groups 생성 (resourceId는 숫자 형식 사용)

        mainFacilityGroupId =
            permissionGroupService.create(
                PermissionGroupCreateRequest(
                    "주요 시설 관리 그룹",
                    null,
                    listOf(PermissionRequest("FACILITY", listOf("1", "2"))),
                ),
            )
        subFacilityGroupId =
            permissionGroupService.create(
                PermissionGroupCreateRequest(
                    "보조 시설 관리 그룹",
                    null,
                    listOf(PermissionRequest("FACILITY", mutableListOf("3"))),
                ),
            )
        deviceCategoryGroupId =
            permissionGroupService.create(
                PermissionGroupCreateRequest(
                    "장비 분류 조회 그룹",
                    null,
                    listOf(PermissionRequest("DEVICE_CATEGORY", mutableListOf("1", "2"))),
                ),
            )

        // 2. Roles 생성 및 Permission Groups 할당
        adminRoleId =
            roleService.save(
                RoleCreateRequest(
                    "ADMIN",
                    "관리자",
                    listOf(mainFacilityGroupId, subFacilityGroupId, deviceCategoryGroupId),
                ),
            )
        operatorRoleId =
            roleService.save(
                RoleCreateRequest(
                    "OPERATOR",
                    "운영자",
                    listOf(mainFacilityGroupId),
                ),
            ) // 운영자는 주요 시설(1, 2)만 관리
        viewerRoleId =
            roleService.save(
                RoleCreateRequest(
                    "VIEWER",
                    "조회자",
                    listOf(deviceCategoryGroupId),
                ),
            ) // 조회자는 장비 분류(1, 2)만 조회

        // 3. Users 생성 및 Roles 할당
        adminUserId =
            userService
                .save(
                    UserCreateRequest(
                        "admin",
                        "pw",
                        "관리자 유저",
                        null,
                        null,
                        null,
                        listOf(adminRoleId),
                    ),
                ).id
        operatorUserId =
            userService
                .save(
                    UserCreateRequest(
                        "operator",
                        "pw",
                        "운영자 유저",
                        null,
                        null,
                        null,
                        listOf(operatorRoleId),
                    ),
                ).id

        em.flush()
        em.clear()
    }

    @Nested
    @DisplayName("전체 라이프사이클 시나리오")
    internal inner class FullLifecycleScenario {
        @Test
        @DisplayName("PermissionGroup 수정 → Role 수정 → User 수정까지 데이터 정합성 유지")
        fun fullLifecycle_shouldMaintainConsistency() {
            // === STEP 1: PermissionGroup의 권한 내용 변경 ===
            val operator = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertTrue(operator.canAccess("FACILITY", "1"), "초기 상태: 1번 시설 접근 가능")
            Assertions.assertFalse(operator.canAccess("FACILITY", "3"), "초기 상태: 3번 시설 접근 불가")

            // WHEN: '주요 시설 관리 그룹'의 권한을 ID 1,2에서 ID 2,3으로 변경
            permissionGroupService.update(
                mainFacilityGroupId,
                PermissionGroupUpdateRequest(
                    "주요 시설 관리 그룹 v2",
                    null,
                    listOf(PermissionRequest("FACILITY", mutableListOf("2", "3"))),
                ),
            )
            em.flush()
            em.clear()

            // THEN: operatorUser의 권한이 즉시 변경되어야 함
            val operatorAfterStep1 = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertFalse(operatorAfterStep1.canAccess("FACILITY", "1"), "1번 시설 권한은 사라져야 함")
            Assertions.assertTrue(operatorAfterStep1.canAccess("FACILITY", "2"), "2번 시설 권한은 유지되어야 함")
            Assertions.assertTrue(operatorAfterStep1.canAccess("FACILITY", "3"), "3번 시설 권한이 생겨야 함")

            // === STEP 2: Role에 할당된 PermissionGroup 변경 ===
            // WHEN: OPERATOR 역할에 '장비 분류 조회 그룹'을 추가
            roleService.update(
                operatorRoleId,
                RoleUpdateRequest("운영자+", null, listOf(mainFacilityGroupId, deviceCategoryGroupId)),
            )
            em.flush()
            em.clear()

            // THEN: operatorUser의 권한이 다시 변경되어야 함
            val operatorAfterStep2 = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertTrue(operatorAfterStep2.canAccess("FACILITY", "3"), "시설 관리 권한은 유지되어야 함")
            Assertions.assertTrue(operatorAfterStep2.canAccess("DEVICE_CATEGORY", "1"), "장비 분류 조회 권한이 생겨야 함")

            // === STEP 3: User에게 할당된 Role 변경 ===
            // WHEN: operatorUser를 운영자(OPERATOR)에서 조회자(VIEWER)로 강등
            userService.update(
                operatorUserId,
                UserUpdateRequest(null, null, null, null, listOf(viewerRoleId)),
            )
            em.flush()
            em.clear()

            // THEN: operatorUser는 이제 VIEWER의 권한만 가져야 함
            val operatorAfterStep3 = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertFalse(operatorAfterStep3.canAccess("FACILITY", "3"), "시설 관리 권한은 없어져야 함")
            Assertions.assertTrue(operatorAfterStep3.canAccess("DEVICE_CATEGORY", "1"), "장비 분류 조회 권한만 남아야 함")
        }
    }

    @Nested
    @DisplayName("최종 권한 검증(Access Control) 시나리오")
    internal inner class AccessControlScenario {
        @Test
        @DisplayName("각 사용자는 자신의 역할에 할당된 권한에만 정확히 접근할 수 있어야 한다")
        fun users_shouldOnlyAccessTheirPermittedResources() {
            // GIVEN
            val admin = userRepository.findWithGraphById(adminUserId)!!
            val operator = userRepository.findWithGraphById(operatorUserId)!!
            val viewerUserId =
                userService
                    .save(
                        UserCreateRequest(
                            "viewer",
                            "pw",
                            "조회자 유저",
                            null,
                            null,
                            null,
                            listOf(viewerRoleId),
                        ),
                    ).id
            em.flush()
            em.clear()
            val viewer = userRepository.findWithGraphById(viewerUserId)!!

            // THEN
            // 1. 관리자(ADMIN)는 모든 권한을 가짐 (canAccess의 특별 로직 검증)
            Assertions.assertTrue(admin.canAccess("FACILITY", "1"))
            Assertions.assertTrue(admin.canAccess("FACILITY", "999")) // 존재하지 않는 ID도 통과
            Assertions.assertTrue(admin.canAccess("DEVICE_CATEGORY", "1"))
            Assertions.assertTrue(admin.canAccess("INVALID_RESOURCE", "ACTION")) // 정의되지 않은 리소스도 통과

            // 2. 운영자(OPERATOR)는 '주요 시설 관리 그룹'의 권한만 가짐
            Assertions.assertTrue(operator.canAccess("FACILITY", "1"))
            Assertions.assertTrue(operator.canAccess("FACILITY", "2"))
            Assertions.assertFalse(operator.canAccess("FACILITY", "3"), "보조 시설 권한은 없어야 함")
            Assertions.assertFalse(operator.canAccess("DEVICE_CATEGORY", "1"), "장비 분류 권한은 없어야 함")

            // 3. 조회자(VIEWER)는 '장비 분류 조회 그룹'의 권한만 가짐
            Assertions.assertTrue(viewer.canAccess("DEVICE_CATEGORY", "1"))
            Assertions.assertTrue(viewer.canAccess("DEVICE_CATEGORY", "2"))
            Assertions.assertFalse(viewer.canAccess("FACILITY", "1"), "시설 권한은 없어야 함")
        }
    }

    @Nested
    @DisplayName("삭제 시나리오 (Deletion Scenarios)")
    internal inner class DeletionScenario {
        @Test
        @DisplayName("PermissionGroup 삭제 시, 해당 그룹을 포함하는 Role과 User의 권한이 자동으로 철회되어야 한다")
        fun whenPermissionGroupIsDeleted_accessShouldBeRevoked() {
            // GIVEN: operator 사용자는 mainFacilityGroupId를 통해 "FACILITY:1" 접근 권한이 있음
            val operatorBeforeDelete = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertTrue(operatorBeforeDelete.canAccess("FACILITY", "1"), "삭제 전, 시설 접근이 가능해야 합니다.")

            // WHEN: '주요 시설 관리 그룹'(mainFacilityGroupId)을 삭제
            permissionGroupService.delete(mainFacilityGroupId)
            em.flush()
            em.clear()

            // THEN:
            // 1. Role과 PermissionGroup의 매핑(RolePermission)이 사라졌는지 확인
            val count =
                rolePermissionRepository.findAll().count { it.permissionGroup.id == mainFacilityGroupId }
            Assertions.assertEquals(0, count, "삭제된 PermissionGroup과 연결된 RolePermission 레코드는 없어야 합니다")

            // 2. operator 사용자의 "FACILITY:1" 접근 권한이 사라졌는지 확인
            val operatorAfterDelete = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertFalse(
                operatorAfterDelete.canAccess("FACILITY", "1"),
                "PermissionGroup 삭제 후, 시설 접근은 불가능해야 합니다.",
            )

            // 3. ADMIN 사용자는 여전히 모든 권한을 가져야 함 (특별 케이스)
            val admin = userRepository.findWithGraphById(adminUserId)!!
            Assertions.assertTrue(admin.canAccess("FACILITY", "1"), "ADMIN은 PermissionGroup 삭제와 무관하게 접근 가능해야 합니다.")
        }

        @Test
        @DisplayName("Role 삭제 시, 해당 Role을 가진 User의 권한이 철회되고 User와 Role의 연결이 끊어져야 한다")
        fun whenRoleIsDeleted_userLosesPermissions() {
            // GIVEN: operator 사용자는 operatorRoleId를 통해 "FACILITY:1" 접근 권한이 있음
            val operatorBeforeDelete = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertEquals(1, operatorBeforeDelete.getRoles().size, "삭제 전, 사용자는 1개의 역할을 가져야 합니다.")
            Assertions.assertTrue(operatorBeforeDelete.canAccess("FACILITY", "1"), "삭제 전, 시설 접근이 가능해야 합니다.")

            // WHEN: 'OPERATOR' 역할(operatorRoleId)을 삭제
            roleService.delete(operatorRoleId)
            em.flush()
            em.clear()

            // THEN:
            // 1. User와 Role의 매핑(UserRole)이 사라졌는지 확인
            val count =
                userRepository
                    .findWithGraphById(operatorBeforeDelete.id!!)!!
                    .userRoles.size
                    .toLong()
            Assertions.assertEquals(0, count, "삭제된 Role과 연결된 UserRole 레코드는 없어야 합니다.")

            // 2. operator 사용자의 권한이 모두 사라졌는지 확인
            val operatorAfterDelete = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertEquals(0, operatorAfterDelete.getRoles().size, "Role 삭제 후, 사용자는 역할을 가지지 않아야 합니다.")
            Assertions.assertFalse(operatorAfterDelete.canAccess("FACILITY", "1"), "Role 삭제 후, 시설 접근은 불가능해야 합니다.")
        }

        @Test
        @DisplayName("User 삭제 시, User와 Role의 연결(UserRole)만 삭제되고 다른 데이터는 영향을 받지 않아야 한다")
        fun whenUserIsDeleted_onlyUserRelatedDataShouldBeRemoved() {
            // GIVEN: operatorUserId가 존재하며, operatorRoleId와 연결되어 있음
            val initialUserRoleCount = userRoleRepository.count()
            Assertions.assertTrue(userRepository.existsById(operatorUserId), "삭제 전, 사용자가 존재해야 합니다.")

            // WHEN: 'operator' 사용자(operatorUserId)를 삭제
            userService.delete(operatorUserId)
            em.flush()
            em.clear()

            // THEN:
            // 1. 사용자가 실제로 삭제되었는지 확인
            Assertions.assertFalse(userRepository.existsById(operatorUserId), "사용자는 성공적으로 삭제되어야 합니다.")

            // 2. UserRole 매핑이 1개 줄었는지 확인
            Assertions.assertEquals(
                initialUserRoleCount - 1,
                userRoleRepository.count(),
                "UserRole 레코드가 1개 줄어야 합니다.",
            )

            // 3. Role과 PermissionGroup은 영향을 받지 않았는지 확인
            Assertions.assertTrue(roleRepository.existsById(operatorRoleId), "Role은 삭제되지 않아야 합니다.")
            Assertions.assertTrue(
                permissionGroupRepository.existsById(mainFacilityGroupId),
                "PermissionGroup은 삭제되지 않아야 합니다.",
            )
        }
    }

    @Nested
    @DisplayName("경계값 및 특수 시나리오 (Edge & Special Cases)")
    internal inner class EdgeCaseScenario {
        @Test
        @DisplayName("User에게서 모든 Role을 제거했을 때, 권한이 모두 사라져야 한다")
        fun whenAllRolesRemovedFromUser_shouldHaveNoPermissions() {
            // GIVEN: operator 사용자는 권한을 가지고 있음
            Assertions.assertTrue(userRepository.findWithGraphById(operatorUserId)!!.canAccess("FACILITY", "1"))

            // WHEN: 사용자 업데이트 시 빈 Role ID 리스트를 전달
            userService.update(operatorUserId, UserUpdateRequest(null, null, null, null, mutableListOf()))
            em.flush()
            em.clear()

            // THEN: 사용자는 더 이상 어떠한 권한도 가지지 않음
            val user = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertFalse(user.canAccess("FACILITY", "1"))
            Assertions.assertFalse(user.canAccess("FACILITY", "2"))
            Assertions.assertEquals(0, user.getRoles().size)
        }

        @Test
        @DisplayName("Role에 할당된 모든 PermissionGroup을 제거했을 때, 해당 Role을 가진 User의 권한이 사라져야 한다")
        fun whenAllPermissionGroupsRemovedFromRole_userShouldLoseAccess() {
            // GIVEN: operator 사용자는 operatorRoleId를 통해 권한을 가지고 있음
            Assertions.assertTrue(userRepository.findWithGraphById(operatorUserId)!!.canAccess("FACILITY", "1"))

            // WHEN: Role 업데이트 시 빈 PermissionGroup ID 리스트를 전달
            roleService.update(operatorRoleId, RoleUpdateRequest("OPERATOR", null, mutableListOf()))
            em.flush()
            em.clear()

            // THEN: operator 사용자의 권한이 사라져야 함
            val user = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertFalse(user.canAccess("FACILITY", "1"))
        }

        @Test
        @DisplayName("아무런 Role 없이 User를 생성하고, 나중에 Role을 할당했을 때 권한이 정상적으로 부여되어야 한다")
        fun whenUserCreatedWithoutRole_thenAssignRole_shouldGrantPermissions() {
            // WHEN: Role 없이 사용자 생성
            val createRequest =
                UserCreateRequest("newUser", "pw", "신규 유저", null, null, null, mutableListOf())
            val newUserId = userService.save(createRequest).id
            em.flush()
            em.clear()

            // THEN: 처음에는 아무 권한이 없음
            val newUser = userRepository.findWithGraphById(newUserId)!!
            Assertions.assertFalse(newUser.canAccess("FACILITY", "1"))

            // WHEN: 나중에 VIEWER Role 할당
            userService.update(
                newUserId,
                UserUpdateRequest(null, null, null, null, listOf(viewerRoleId)),
            )
            em.flush()
            em.clear()

            // THEN: VIEWER의 권한을 획득해야 함
            val updatedUser = userRepository.findWithGraphById(newUserId)!!
            Assertions.assertTrue(updatedUser.canAccess("DEVICE_CATEGORY", "1"))
            Assertions.assertFalse(updatedUser.canAccess("FACILITY", "1"))
        }
    }

    @Nested
    @DisplayName("다중 역할 및 권한 중첩 시나리오 (Multiple Roles & Overlapping Permissions)")
    internal inner class MultipleRolesScenario {
        @Test
        @DisplayName("사용자가 여러 Role을 가질 때, 모든 Role의 권한을 합산하여 가져야 한다")
        fun whenUserHasMultipleRoles_shouldAggregateAllPermissions() {
            // GIVEN: operator(주요 시설)와 viewer(장비 분류) 역할을 모두 가지는 새로운 사용자 생성
            val multiRoleUserId =
                userService
                    .save(
                        UserCreateRequest(
                            "multiRoleUser",
                            "pw",
                            "다중 역할 유저",
                            null,
                            null,
                            null,
                            listOf(operatorRoleId, viewerRoleId),
                        ),
                    ).id
            em.flush()
            em.clear()

            // WHEN: 사용자의 권한을 검증
            val multiRoleUser = userRepository.findWithGraphById(multiRoleUserId)!!

            // THEN: 두 역할의 권한을 모두 가져야 함
            Assertions.assertTrue(multiRoleUser.canAccess("FACILITY", "1"), "OPERATOR 역할의 주요 시설 권한이 있어야 합니다.")
            Assertions.assertTrue(multiRoleUser.canAccess("FACILITY", "2"), "OPERATOR 역할의 주요 시설 권한이 있어야 합니다.")
            Assertions.assertTrue(multiRoleUser.canAccess("DEVICE_CATEGORY", "1"), "VIEWER 역할의 장비 분류 권한이 있어야 합니다.")
            Assertions.assertTrue(multiRoleUser.canAccess("DEVICE_CATEGORY", "2"), "VIEWER 역할의 장비 분류 권한이 있어야 합니다.")

            // AND: 두 역할에 모두 없는 권한은 없어야 함
            Assertions.assertFalse(multiRoleUser.canAccess("FACILITY", "3"), "어떤 역할에도 없는 보조 시설 권한은 없어야 합니다.")
        }

        @Test
        @DisplayName("Role에 할당된 여러 PermissionGroup이 중복된 권한을 포함해도, canAccess는 정상 동작해야 한다")
        fun whenRoleHasOverlappingPermissions_canAccessShouldWorkCorrectly() {
            // GIVEN:
            // "FACILITY:2" 권한을 중복으로 포함하는 새로운 PermissionGroup 생성
            val overlappingPermission =
                PermissionRequest("FACILITY", mutableListOf("2", "4"))
            val overlappingGroupId =
                permissionGroupService.create(
                    PermissionGroupCreateRequest("중복 권한 그룹", null, listOf(overlappingPermission)),
                )

            // OPERATOR 역할에 이 그룹을 추가 (기존 '주요 시설 관리 그룹'과 "FACILITY:2"가 겹침)
            roleService.update(
                operatorRoleId,
                RoleUpdateRequest(null, null, listOf(mainFacilityGroupId, overlappingGroupId)),
            )
            em.flush()
            em.clear()

            // WHEN: operator 사용자의 권한을 검증
            val operator = userRepository.findWithGraphById(operatorUserId)!!

            // THEN: 중복 여부와 관계없이 권한을 올바르게 판단해야 함
            Assertions.assertTrue(operator.canAccess("FACILITY", "1"), "기존 그룹의 권한")
            Assertions.assertTrue(operator.canAccess("FACILITY", "2"), "중복된 권한")
            Assertions.assertTrue(operator.canAccess("FACILITY", "4"), "새로 추가된 그룹의 권한")
            Assertions.assertFalse(operator.canAccess("FACILITY", "3"), "여전히 없는 권한")
        }
    }
}
