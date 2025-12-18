package com.pluxity.user

import com.pluxity.building.Building
import com.pluxity.building.BuildingRepository
import com.pluxity.config.MockBeansConfig
import com.pluxity.facility.FacilityService
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionGroupService
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionRequest
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.UserRoleUpdateRequest
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.User
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
import com.pluxity.user.service.RoleService
import com.pluxity.user.service.UserService
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
@ActiveProfiles("test")
internal class UserRolePermissionIntegrationTest
    @Autowired
    constructor(
        private val userService: UserService,
        private val roleService: RoleService,
        private val permissionGroupService: PermissionGroupService,
        private val facilityService: FacilityService,
        private val userRepository: UserRepository,
        private val roleRepository: RoleRepository,
        private val buildingRepository: BuildingRepository,
        private val em: EntityManager,
    ) {
        lateinit var adminUser: User
        lateinit var editorUser: User
        lateinit var adminRole: Role
        val buildings: MutableList<Building> = mutableListOf()

        @BeforeEach
        fun setUp() {
            // 1. 기본 역할 생성
            adminRole = roleRepository.save(Role(name = "ADMIN", description = "관리자"))

            // 2. 기본 사용자 생성
            adminUser =
                userRepository.save(
                    User(
                        username = "admin",
                        password = "pw",
                        code = "",
                        name = "관리자",
                    ),
                )
            adminUser.addRole(adminRole)

            editorUser =
                userRepository.save(
                    User(
                        username = "editor",
                        password = "pw",
                        code = "",
                        name = "편집자",
                    ),
                )

            // 3. 테스트용 리소스(Facility) 5개 생성
            buildings.clear()
            buildings.addAll(
                (1..5).map { i ->
                    buildingRepository.save(Building("Building $i", null))
                },
            )

            em.flush()
            em.clear()
        }

        private fun setAuthentication(user: User) {
            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = UsernamePasswordAuthenticationToken(user.username, null, null)
            SecurityContextHolder.setContext(context)
        }

        @Test
        @DisplayName("특정 시설 접근 권한을 가진 역할을 부여받은 사용자는, 허가된 시설만 조회할 수 있다")
        fun userWithSpecificRole_canOnlyAccessPermittedResources() {
            // === GIVEN: '편집자' 사용자에게 1번, 3번 시설에 대한 접근 권한만 부여 ===

            // 1. 관리자로 로그인

            setAuthentication(adminUser)

            // 2. [수정] 1번, 3번 시설에 대한 PermissionGroup을 생성합니다.
            val createGroup1Request =
                PermissionGroupCreateRequest(
                    "1번 시설 그룹",
                    "1번 시설 접근 권한",
                    listOf(
                        PermissionRequest(
                            ResourceType.FACILITY.name,
                            listOf(buildings[0].id.toString()),
                        ),
                    ),
                )
            val createGroup3Request =
                PermissionGroupCreateRequest(
                    "3번 시설 그룹",
                    "3번 시설 접근 권한",
                    listOf(
                        PermissionRequest(
                            ResourceType.FACILITY.name,
                            listOf(buildings[2].id.toString()),
                        ),
                    ),
                )

            val group1Id = permissionGroupService.create(createGroup1Request)
            val group3Id = permissionGroupService.create(createGroup3Request)

            val permittedGroupIds = listOf(group1Id, group3Id)

            // 3. [수정] "시설 관리자" 역할을 생성하면서 위에서 생성한 PermissionGroup들의 ID 목록을 전달합니다.
            val createRoleRequest =
                RoleCreateRequest("시설 관리자", "1, 3번 시설 접근 가능", permittedGroupIds)
            val authentication = UsernamePasswordAuthenticationToken("testUser", null, null)
            val newRoleId = roleService.save(createRoleRequest, authentication)

            // 4. 생성된 "시설 관리자" 역할을 '편집자' 사용자에게 할당합니다.
            userService.updateUserRoles(
                editorUser.requiredId(),
                UserRoleUpdateRequest(listOf(newRoleId)),
            )

            em.flush()
            em.clear()

            // === WHEN: '편집자' 사용자로 로그인하여 시설 목록을 조회 ===
            setAuthentication(editorUser)

            // FacilityService가 Building 목록을 반환한다고 가정하고 수정
            val accessibleBuildings = facilityService.findAll()

            // === THEN: 오직 허가된 시설만 조회되어야 함 ===
            Assertions.assertThat(accessibleBuildings).hasSize(2)

            val permittedBuildingIds = listOf(buildings[0].id, buildings[2].id)
            val accessibleIds = accessibleBuildings.map { it.id }
            Assertions.assertThat(accessibleIds).containsExactlyInAnyOrderElementsOf(permittedBuildingIds)

            // 추가 검증: 허가된 시설(1번)에 ID로 직접 접근하면 성공해야 합니다.
            val permittedId = buildings[0].requiredId()
            // FacilityService가 Building ID로 조회하는 메서드가 있다고 가정
            org.junit.jupiter.api.Assertions.assertDoesNotThrow {
                facilityService.findById(permittedId)
            }

            // 추가 검증: 허가되지 않은 시설(2번)에 ID로 직접 접근하면 예외가 발생해야 합니다.
            val forbiddenId = buildings[1].requiredId()
            org.junit.jupiter.api.Assertions.assertThrows(
                CustomException::class.java,
                { facilityService.findById(forbiddenId) },
                "허가되지 않은 리소스 접근 시 CustomException이 발생해야 합니다.",
            )
        }
    }
