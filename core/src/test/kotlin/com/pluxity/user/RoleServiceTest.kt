package com.pluxity.user

import com.pluxity.building.Building
import com.pluxity.building.BuildingRepository
import com.pluxity.config.MockBeansConfig
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionGroupRepository
import com.pluxity.permission.PermissionGroupService
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionRequest
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleUpdateRequest
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RolePermission
import com.pluxity.user.repository.RolePermissionRepository
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.service.RoleService
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
@ActiveProfiles("test")
internal class RoleServiceTest
    @Autowired
    constructor(
        private val roleService: RoleService,
        private val permissionGroupService: PermissionGroupService, // PermissionService -> PermissionGroupService
        private val permissionGroupRepository: PermissionGroupRepository, // 추가
        private val buildingRepository: BuildingRepository,
        private val em: EntityManager,
        private val roleRepository: RoleRepository,
        private val rolePermissionRepository: RolePermissionRepository,
    ) {
        val buildings: MutableList<Building> = mutableListOf()

        // permissionIds -> permissionGroupIds
        val permissionGroupIds: MutableList<Long> = mutableListOf()

        @BeforeEach
        fun setUp() {
            // 테스트에 사용할 건물(리소스) 생성
            buildings.clear()
            buildings.addAll(
                (1..3).map { i ->
                    buildingRepository.save(Building("Building $i", null))
                },
            )

            // [수정] 테스트에 사용할 권한 그룹(PermissionGroup)을 미리 생성
            permissionGroupIds.clear()

            buildings.forEach { building: Building ->
                // 각 건물 ID에 대해 하나의 권한을 가진 그룹을 생성
                val request =
                    PermissionGroupCreateRequest(
                        "Building ${building.id} Group",
                        "Description for ${building.name}",
                        listOf(
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                listOf(building.id.toString()),
                            ),
                        ),
                    )
                val groupId = permissionGroupService.create(request)
                permissionGroupIds.add(groupId)
            }

            em.flush()
            em.clear()
        }

        @Test
        @DisplayName("새로운 Role을 권한 그룹과 함께 생성하고, 생성된 Role을 서비스로 조회하여 검증한다")
        fun save_withPermissionGroups_andVerifyWithService() {
            // GIVEN
            // 1번, 2번 건물에 대한 권한 그룹 ID만 사용하여 Role 생성
            val initialGroupIds = listOf(permissionGroupIds[0], permissionGroupIds[1])
            val createRequest =
                RoleCreateRequest("Test Role", "A role for testing", initialGroupIds)

            // WHEN
            val authentication = UsernamePasswordAuthenticationToken("testUser", null, null)
            val roleId = roleService.save(createRequest, authentication)
            em.flush()
            em.clear()

            // THEN: Service를 통해 조회하여 검증
            val response = roleService.findById(roleId)

            Assertions.assertThat(response.name).isEqualTo("Test Role")
            Assertions.assertThat(response.description).isEqualTo("A role for testing")

            // Role이 가진 PermissionGroup 목록을 검증
            // RoleResponse가 PermissionGroup ID 목록을 직접 반환한다고 가정
            // (만약 아니라면, Role 엔티티를 직접 조회해서 확인해야 함)
            val responseGroupIds =
                roleService
                    .findRoleById(roleId)
                    .rolePermissions
                    .map { it.permissionGroup.id }

            Assertions.assertThat(responseGroupIds).hasSize(2)
            Assertions.assertThat(responseGroupIds).containsExactlyInAnyOrderElementsOf(initialGroupIds)
        }

        @Test
        @DisplayName("ID로 Role 조회 시, 할당된 모든 권한 그룹의 상세 권한 정보까지 포함하여 반환한다")
        fun findById_returnsRoleWithAllPermissionsInGroups() {
            // GIVEN
            val initialGroupIds = listOf(permissionGroupIds[0], permissionGroupIds[1])
            val createRequest =
                RoleCreateRequest(
                    "Test Role",
                    "For findById test",
                    initialGroupIds,
                    listOf(ResourceType.FACILITY, ResourceType.DEVICE_CATEGORY),
                )
            val authentication = UsernamePasswordAuthenticationToken("testUser", null, null)
            val roleId = roleService.save(createRequest, authentication)
            em.flush()
            em.clear()

            // WHEN
            val response = roleService.findById(roleId)

            // THEN
            Assertions.assertThat(response.name).isEqualTo("Test Role")
            Assertions.assertThat(response.permissions).isNotNull()
            // 각 그룹에 Permission이 1개씩 있으므로, 총 2개의 Permission이 조회되어야 함
            Assertions.assertThat(response.permissions).hasSize(2)
            Assertions
                .assertThat(response.globalPolicyTypes)
                .containsExactlyInAnyOrder(ResourceType.FACILITY, ResourceType.DEVICE_CATEGORY)

            val responseResourceIds =
                response.permissions
                    .flatMap { group -> group.permissions }
                    .flatMap { perm -> perm.permissions.map { it.resourceId } }

            Assertions
                .assertThat(responseResourceIds)
                .containsExactlyInAnyOrder(
                    buildings[0].id.toString(),
                    buildings[1].id.toString(),
                )
        }

        @Test
        @DisplayName("Role 업데이트 후, 서비스를 통해 조회하여 변경사항과 권한 그룹 동기화를 검증한다")
        fun update_andVerifyWithService() {
            // GIVEN: 1, 2번 건물 권한 그룹을 가진 Role을 먼저 생성
            val authentication = UsernamePasswordAuthenticationToken("testUser", null, null)
            val roleId =
                roleService.save(
                    RoleCreateRequest(
                        "Initial Role",
                        "Desc",
                        listOf(permissionGroupIds[0], permissionGroupIds[1]),
                        listOf(ResourceType.FACILITY, ResourceType.DEVICE_CATEGORY),
                    ),
                    authentication,
                )
            em.flush()
            em.clear()

            // 업데이트 요청: 1번은 삭제, 2번은 유지, 3번은 새로 추가 -> 최종 권한 그룹은 2, 3번
            val updatedGroupIdList = listOf(permissionGroupIds[1], permissionGroupIds[2])
            val updateRequest =
                RoleUpdateRequest(
                    "Updated Role",
                    "Updated Description",
                    updatedGroupIdList,
                    listOf(ResourceType.CCTV),
                )

            // WHEN
            roleService.update(roleId, updateRequest)
            em.flush()
            em.clear()

            // THEN: Service를 통해 조회하여 검증
            val response = roleService.findById(roleId)

            Assertions.assertThat(response.name).isEqualTo("Updated Role")
            Assertions.assertThat(response.description).isEqualTo("Updated Description")

            // 최종 권한이 올바르게 동기화되었는지 검증 (Permission 2개 확인)
            Assertions.assertThat(response.permissions).hasSize(2)
            Assertions
                .assertThat(response.globalPolicyTypes)
                .containsExactly(ResourceType.CCTV)
            val finalResourceIds =
                response.permissions
                    .flatMap { group -> group.permissions }
                    .flatMap { perm -> perm.permissions.map { it.resourceId } }

            Assertions
                .assertThat(finalResourceIds)
                .containsExactlyInAnyOrder(
                    buildings[1].id.toString(),
                    buildings[2].id.toString(),
                )
        }

        @Test
        @DisplayName("Role 삭제 후, 서비스를 통해 조회 시 예외가 발생하는지 검증한다")
        fun delete_andVerifyDeletionWithService() {
            // GIVEN
            val role =
                roleRepository.save(Role(name = "Deletable Role", description = "Desc"))
            em.flush()
            em.clear()
            val permissionGroup =
                permissionGroupService.findPermissionGroupById(permissionGroupIds.first())
            val newRolePermissions = listOf(RolePermission(role = role, permissionGroup = permissionGroup))
            rolePermissionRepository.saveAll(newRolePermissions)
            newRolePermissions.forEach { rolePermission: RolePermission -> role.addRolePermission(rolePermission) }
            val roleId = role.requiredId

            Assertions.assertThat(roleService.findById(roleId)).isNotNull()

            val initialGroupCount = permissionGroupRepository.count()

            // WHEN
            roleService.delete(roleId)
            em.flush()
            em.clear()

            // THEN
            assertThrows<CustomException> { roleService.findById(roleId) }

            // [중요] PermissionGroup 엔티티 자체는 삭제되지 않고 그대로 남아있어야 함을 검증
            Assertions.assertThat(permissionGroupRepository.count()).isEqualTo(initialGroupCount)
        }

        @Test
        @DisplayName("존재하지 않는 Role ID로 조회 시 예외가 발생한다")
        fun findById_withNonExistentId_throwsException() {
            // GIVEN
            val nonExistentId = 9999L

            // WHEN & THEN
            assertThrows<CustomException> { roleService.findById(nonExistentId) }
        }
    }
