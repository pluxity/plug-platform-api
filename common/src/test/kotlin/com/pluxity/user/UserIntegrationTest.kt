package com.pluxity.user

import com.pluxity.config.MockBeansConfig
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionGroupRepository
import com.pluxity.permission.PermissionGroupService
import com.pluxity.permission.PermissionRepository
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionRequest
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleUpdateRequest
import com.pluxity.user.dto.UserCreateRequest
import com.pluxity.user.dto.UserUpdateRequest
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.User
import com.pluxity.user.repository.RolePermissionRepository
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
import com.pluxity.user.repository.UserRoleRepository
import com.pluxity.user.service.RoleService
import com.pluxity.user.service.UserService
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.transaction.annotation.Transactional
import kotlin.properties.Delegates

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class UserIntegrationTest
    @Autowired
    constructor(
        private val userService: UserService,
        private val roleService: RoleService,
        private val permissionGroupService: PermissionGroupService, // PermissionService -> PermissionGroupService
        private val userRepository: UserRepository,
        private val userRoleRepository: UserRoleRepository,
        private val roleRepository: RoleRepository,
        private val rolePermissionRepository: RolePermissionRepository,
        private val permissionGroupRepository: PermissionGroupRepository, // 추가
        private val permissionRepository: PermissionRepository, // 추가
        private val em: EntityManager,
    ) {
        // 테스트 전체에서 사용할 고정된 ID
        private var adminUserId by Delegates.notNull<Long>()
        private var operatorUserId by Delegates.notNull<Long>()
        private var adminRoleId by Delegates.notNull<Long>()
        private var operatorRoleId by Delegates.notNull<Long>()
        private var viewerRoleId by Delegates.notNull<Long>()

        // Permission ID -> PermissionGroup ID
        private var userManageGroupId by Delegates.notNull<Long>()
        private var facilityReadGroupId by Delegates.notNull<Long>()
        private var facilityEditGroupId by Delegates.notNull<Long>()

        /** 각 테스트 실행 전, 복잡하게 얽힌 상태를 미리 설정합니다. (PermissionGroup 중심 구조로 변경)  */
        @BeforeEach
        fun setUp() {
            // 모든 테이블 초기화 (참조 무결성 순서 고려)
            userRoleRepository.deleteAllInBatch()
            rolePermissionRepository.deleteAllInBatch()
            permissionRepository.deleteAllInBatch()
            permissionGroupRepository.deleteAllInBatch()
            userRepository.deleteAllInBatch()
            roleRepository.deleteAllInBatch()

            em.flush()
            em.clear()

            // 1. PermissionGroup 생성
            userManageGroupId =
                permissionGroupService.create(
                    PermissionGroupCreateRequest(
                        "사용자 관리 그룹",
                        "모든 사용자 관리 권한",
                        listOf(
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                mutableListOf("*"),
                            ),
                        ),
                    ),
                )
            facilityReadGroupId =
                permissionGroupService.create(
                    PermissionGroupCreateRequest(
                        "시설 조회 그룹",
                        "시설 조회 권한",
                        listOf(
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                mutableListOf("READ"),
                            ),
                        ),
                    ),
                )
            facilityEditGroupId =
                permissionGroupService.create(
                    PermissionGroupCreateRequest(
                        "시설 수정 그룹",
                        "시설 수정 권한",
                        listOf(
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                mutableListOf("EDIT"),
                            ),
                        ),
                    ),
                )

            // 2. Role 생성 및 PermissionGroup 할당
            adminRoleId =
                roleService.save(
                    RoleCreateRequest(
                        name = "ADMIN",
                        description = "관리자",
                        permissionGroupIds = listOf(userManageGroupId, facilityReadGroupId, facilityEditGroupId),
                    ),
                    UsernamePasswordAuthenticationToken("testUser", null, null),
                )
            operatorRoleId =
                roleService.save(
                    RoleCreateRequest(
                        name = "OPERATOR",
                        description = "운영자",
                        permissionGroupIds = listOf(facilityReadGroupId, facilityEditGroupId),
                    ),
                    UsernamePasswordAuthenticationToken("testUser", null, null),
                )
            viewerRoleId =
                roleService.save(
                    RoleCreateRequest(
                        name = "VIEWER",
                        description = "조회자",
                        permissionGroupIds = listOf(facilityReadGroupId),
                    ),
                    UsernamePasswordAuthenticationToken("testUser", null, null),
                )

            // 3. User 생성 및 Role 할당
            adminUserId =
                userService
                    .save(
                        UserCreateRequest(
                            "admin",
                            "pw",
                            "Admin User",
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
                            "Operator User",
                            null,
                            null,
                            null,
                            listOf(operatorRoleId),
                        ),
                    ).id

            em.flush()
            em.clear()
        }

        @Test
        @DisplayName("[연쇄 삭제 검증 1] 특정 Role 삭제 시, 해당 Role을 가진 User는 유지되지만 UserRole 연결은 끊어져야 한다")
        fun deleteRole_shouldOnlyRemoveRoleAndUserRoleLink_notUser() {
            // 이 테스트는 Permission 모델 변경과 직접적인 관련이 없으므로, GIVEN 블록을 명확히 재설정하여 그대로 테스트
            // GIVEN
            rolePermissionRepository.deleteAllInBatch()
            userRoleRepository.deleteAllInBatch()
            roleRepository.deleteAllInBatch()
            userRepository.deleteAllInBatch()
            em.flush()
            em.clear()

            val roleToDelete = roleRepository.save(Role(null, "DELETABLE_ROLE", "곧 삭제될 역할"))
            val roleToKeep = roleRepository.save(Role(null, "KEEPER_ROLE", "유지될 역할"))

            val userWithTwoRoles = User(null, "multiRoleUser", "pw", "다중역할사용자", "", null, null)
            userWithTwoRoles.addRole(roleToDelete)
            userWithTwoRoles.addRole(roleToKeep)
            userRepository.save(userWithTwoRoles)

            val userWithOneRole = User(null, "singleRoleUser", "pw", "단일역할사용자", "", null, null)
            userWithOneRole.addRole(roleToDelete)
            userRepository.save(userWithOneRole)

            em.flush()
            em.clear()
            Assertions.assertThat(userRoleRepository.count()).isEqualTo(3)

            // WHEN
            roleService.delete(roleToDelete.id!!)
            em.flush()
            em.clear()

            // THEN
            Assertions.assertThat(roleRepository.findById(roleToDelete.id!!)).isEmpty()
            Assertions.assertThat(roleRepository.findById(roleToKeep.id!!)).isPresent()
            Assertions.assertThat(userRepository.count()).isEqualTo(2)
            Assertions.assertThat(userRoleRepository.count()).isEqualTo(1)

            val survivingUser1 = userRepository.findByUsername("multiRoleUser")!!
            val survivingUser2 = userRepository.findByUsername("singleRoleUser")!!
            Assertions.assertThat(survivingUser1.getRoles()).hasSize(1)
            Assertions.assertThat(survivingUser1.getRoles().first().id).isEqualTo(roleToKeep.id)
            Assertions.assertThat(survivingUser2.getRoles()).isEmpty()
        }

        @Test
        @DisplayName("[연쇄 삭제 검증 2] 특정 PermissionGroup 삭제 시, Role들은 유지되지만 RolePermission 연결은 끊어져야 한다")
        fun deletePermissionGroup_shouldOnlyRemoveGroupAndRolePermissionLink_notRole() {
            // GIVEN
            val initialRoleCount = roleRepository.count()
            val initialRolePermissionCount =
                rolePermissionRepository.count() // ADMIN=3, OPERATOR=2, VIEWER=1 -> 6
            val adminRoleBeforeDelete = roleRepository.findById(adminRoleId).get()
            val operatorRoleBeforeDelete = roleRepository.findById(operatorRoleId).get()
            Assertions.assertThat(adminRoleBeforeDelete.rolePermissions).hasSize(3)
            Assertions.assertThat(operatorRoleBeforeDelete.rolePermissions).hasSize(2)

            // WHEN: 운영자와 관리자 모두 가진 '시설 수정 그룹' 삭제
            permissionGroupService.delete(facilityEditGroupId)
            em.flush()
            em.clear()

            // THEN
            assertThrows<CustomException> {
                permissionGroupService.findById(facilityEditGroupId)
            }
            Assertions.assertThat(roleRepository.count()).isEqualTo(initialRoleCount)
            Assertions.assertThat(rolePermissionRepository.count()).isEqualTo(initialRolePermissionCount - 2)

            val adminRoleAfterDelete = roleRepository.findById(adminRoleId).get()
            val operatorRoleAfterDelete = roleRepository.findById(operatorRoleId).get()
            Assertions.assertThat(adminRoleAfterDelete.rolePermissions).hasSize(2)
            Assertions.assertThat(operatorRoleAfterDelete.rolePermissions).hasSize(1)
        }

        @Test
        @DisplayName("[연쇄 삭제 검증 3] User 삭제 시, User와 UserRole만 삭제되고 Role 자체는 유지되어야 한다")
        fun deleteUser_shouldOnlyRemoveUserAndUserRoleLink_notRole() {
            // 이 테스트는 Permission 모델 변경과 관련 없으므로 그대로 유효
            // GIVEN
            val initialRoleCount = roleRepository.count()
            val initialUserRoleCount = userRoleRepository.count()

            // WHEN
            userService.delete(operatorUserId)
            em.flush()
            em.clear()

            // THEN
            assertThrows<EntityNotFoundException> {
                userService.findById(operatorUserId)
            }
            Assertions.assertThat(userRoleRepository.count()).isEqualTo(initialUserRoleCount - 1)
            Assertions.assertThat(roleRepository.count()).isEqualTo(initialRoleCount)
        }

        @Test
        @DisplayName("[복합 업데이트 1] User의 Role을 완전히 다른 것으로 교체 (OPERATOR -> VIEWER)")
        fun updateUserRole_fromOneToAnother() {
            // 이 테스트는 Permission 모델 변경과 관련 없으므로 그대로 유효
            // GIVEN
            val user = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertThat(user.getRoles()[0].id).isEqualTo(operatorRoleId)

            // WHEN
            val request =
                UserUpdateRequest(null, null, null, null, listOf(viewerRoleId))
            userService.update(operatorUserId, request)
            em.flush()
            em.clear()

            // THEN
            val updatedUser = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertThat(updatedUser.getRoles()).hasSize(1)
            Assertions.assertThat(updatedUser.getRoles()[0].id).isEqualTo(viewerRoleId)
        }

        @Test
        @DisplayName("[복합 업데이트 2] Role의 PermissionGroup 목록을 변경하면 User의 접근 권한이 즉시 변경되어야 한다")
        fun updateRolePermissions_shouldReflectOnAllUsersWithThatRole() {
            // GIVEN
            val operator = userRepository.findWithGraphById(operatorUserId)!!
            // canAccess 메서드를 사용하여 권한 확인
            org.junit.jupiter.api.Assertions
                .assertTrue(operator.canAccess("FACILITY", "EDIT"))

            // WHEN: OPERATOR 역할에서 '시설 수정 그룹'을 제거하고 '사용자 관리 그룹'을 추가
            roleService.update(
                operatorRoleId,
                RoleUpdateRequest(
                    "운영자",
                    "권한 변경된 운영자",
                    listOf(facilityReadGroupId, userManageGroupId),
                ),
            )
            em.flush()
            em.clear()

            // THEN
            val updatedOperator = userRepository.findWithGraphById(operatorUserId)!!
            org.junit.jupiter.api.Assertions
                .assertFalse(updatedOperator.canAccess("FACILITY", "EDIT")) // 수정 권한 없어짐
            org.junit.jupiter.api.Assertions
                .assertTrue(updatedOperator.canAccess("FACILITY", "READ")) // 조회 권한 유지
            // userManageGroupId는 FACILITY ResourceType에 대해 '*' 권한을 가지므로, 아래와 같이 검증
            org.junit.jupiter.api.Assertions
                .assertTrue(updatedOperator.canAccess("FACILITY", "*")) // 사용자 관리 권한 생김
        }

        @Test
        @DisplayName("[전체 시나리오] PermissionGroup 삭제 -> User 역할 변경 -> Role 삭제 -> User 삭제 순으로 실행해도 데이터 정합성이 깨지지 않는다")
        fun fullScenario_deletePermissionThenUpdateRoleThenUpdateUserThenDeleteUser() {
            // === 1. PermissionGroup 삭제 (facility_edit) ===
            permissionGroupService.delete(facilityEditGroupId)
            em.flush()
            em.clear()

            // THEN 1
            val operatorRole1 = roleRepository.findById(operatorRoleId).get()
            Assertions.assertThat(operatorRole1.rolePermissions).hasSize(1)
            val operator1 = userRepository.findWithGraphById(operatorUserId)!!
            // canAccess 메서드를 사용하여 권한 확인
            org.junit.jupiter.api.Assertions
                .assertFalse(operator1.canAccess("FACILITY", "EDIT"))

            // === 2. User의 Role 변경 (operator -> viewer) ===
            userService.update(
                operatorUserId,
                UserUpdateRequest(null, null, null, null, listOf(viewerRoleId)),
            )
            em.flush()
            em.clear()

            // THEN 2
            val operator2 = userRepository.findWithGraphById(operatorUserId)!!
            Assertions.assertThat(operator2.getRoles()[0].name).isEqualTo("VIEWER")
            // canAccess 메서드를 사용하여 권한 확인
            org.junit.jupiter.api.Assertions
                .assertFalse(operator2.canAccess("FACILITY", "EDIT"))
            org.junit.jupiter.api.Assertions
                .assertTrue(operator2.canAccess("FACILITY", "READ"))

            // === 3. Role 삭제 (이제 아무도 쓰지 않는 OPERATOR Role) ===
            roleService.delete(operatorRoleId)
            em.flush()
            em.clear()

            // THEN 3
            assertThrows<EntityNotFoundException> {
                roleService.findById(operatorRoleId)
            }
            Assertions.assertThat(roleRepository.count()).isEqualTo(2)

            // === 4. User 삭제 (operator) ===
            userService.delete(operatorUserId)
            em.flush()
            em.clear()

            // THEN 4
            assertThrows<EntityNotFoundException> {
                userService.findById(operatorUserId)
            }
            Assertions.assertThat(userRepository.count()).isEqualTo(1)
            Assertions.assertThat(userRoleRepository.count()).isEqualTo(1)

            // FINAL: admin 유저와 관련 데이터는 모두 온전해야 함
            Assertions.assertThat(userRepository.findWithGraphById(adminUserId)).isNotNull()
            Assertions.assertThat(roleRepository.findById(adminRoleId)).isPresent()
            Assertions.assertThat(permissionGroupRepository.findById(userManageGroupId)).isPresent()
        }
    }
