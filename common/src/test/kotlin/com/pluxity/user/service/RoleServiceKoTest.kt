package com.pluxity.user.service

import base.entity.withId
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionGroup
import com.pluxity.permission.PermissionGroupService
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleUpdateRequest
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RolePermission
import com.pluxity.user.repository.RolePermissionRepository
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRoleRepository
import com.pluxity.user.service.entity.dummyRole
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.persistence.EntityManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class RoleServiceKoTest :
    BehaviorSpec({
        isolationMode = IsolationMode.InstancePerLeaf

        val roleRepository: RoleRepository = mockk()
        val rolePermissionRepository: RolePermissionRepository = mockk()
        val userRoleRepository: UserRoleRepository = mockk()
        val permissionGroupService: PermissionGroupService = mockk()
        val em: EntityManager = mockk()

        val roleService =
            RoleService(
                roleRepository,
                rolePermissionRepository,
                userRoleRepository,
                permissionGroupService,
                em,
            )

        Given("Role 생성을 진행할 때") {
            When("유효한 요청으로 Role 생성 요청") {
                val createRequest =
                    RoleCreateRequest(
                        name = "Test Role",
                        description = "Test Description",
                        permissionGroupIds = listOf(1L, 2L),
                    )
                val savedRole = dummyRole(name = createRequest.name, description = createRequest.description)
                val permissionGroup1 =
                    PermissionGroup(
                        name = "Group 1",
                        description = "Group 1 Description",
                    )
                val permissionGroup2 =
                    PermissionGroup(
                        name = "Group 2",
                        description = "Group 2 Description",
                    )

                every { roleRepository.save(any()) } returns savedRole
                every { permissionGroupService.findPermissionGroupById(1L) } returns permissionGroup1
                every { permissionGroupService.findPermissionGroupById(2L) } returns permissionGroup2
                every { rolePermissionRepository.saveAll(any<List<RolePermission>>()) } returns listOf()

                Then("성공") {
                    val result = roleService.save(createRequest, UsernamePasswordAuthenticationToken("testUser", null, null))
                    result shouldBe 1L
                }
            }

            When("Permission Group ID가 빈 배열인 요청으로 Role 생성") {
                val createRequest =
                    RoleCreateRequest(
                        name = "Simple Role",
                        description = "Simple Description",
                        permissionGroupIds = emptyList(),
                    )
                val savedRole =
                    dummyRole(
                        id = 2L,
                        name = "Simple Role",
                        description = "Simple Description",
                    )

                every { roleRepository.save(any()) } returns savedRole

                Then("성공") {
                    val result = roleService.save(createRequest, UsernamePasswordAuthenticationToken("testUser", null, null))
                    result shouldBe 2L
                }
            }
        }

        Given("Role 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                val role1 = Role(name = "Role1", description = "Desc1").withId(1L)
                val role2 = Role(name = "Role2", description = "Desc2").withId(2L)

                every { roleRepository.findByAuthIsNotOrderByCreatedAtDesc(any()) } returns listOf(role1, role2)

                Then("정상 조회") {
                    val result = roleService.findAll()
                    result.size shouldBe 2
                    result[0].name shouldBe "Role1"
                    result[1].name shouldBe "Role2"
                }
            }

            When("데이터가 없을 때") {
                every { roleRepository.findByAuthIsNotOrderByCreatedAtDesc(any()) } returns emptyList()

                Then("빈 목록 반환") {
                    val result = roleService.findAll()
                    result.size shouldBe 0
                }
            }
        }

        Given("Role 상세 조회를 진행할 때") {
            When("유효한 아이디로 조회 요청") {
                val role = dummyRole(id = 1L, name = "Test Role", description = "Test Description")

                every { roleRepository.findWithInfoById(1L) } returns role

                Then("정상 조회") {
                    val result = roleService.findById(1L)
                    result.id shouldBe 1L
                    result.name shouldBe "Test Role"
                    result.description shouldBe "Test Description"
                }
            }

            When("없는 아이디로 조회 요청") {
                every { roleRepository.findWithInfoById(999L) } returns null

                Then("CustomException 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        roleService.findById(999L)
                    }.message shouldBe "ID가 999인 Role을 찾을 수 없습니다."
                }
            }
        }

        Given("Role 업데이트를 진행할 때") {
            When("이름과 설명만 변경하는 요청") {
                val role = dummyRole(id = 1L, name = "Old Name", description = "Old Description")
                val updateRequest =
                    RoleUpdateRequest(
                        name = "New Name",
                        description = "New Description",
                        permissionGroupIds = null,
                    )

                every { roleRepository.findWithInfoById(1L) } returns role

                Then("성공적으로 업데이트") {
                    roleService.update(1L, updateRequest)

                    role.name shouldBe "New Name"
                    role.description shouldBe "New Description"
                }
            }

            When("Permission Group을 추가하는 요청") {
                val role = dummyRole(id = 1L, name = "Test Role", description = "Test Description")
                val updateRequest =
                    RoleUpdateRequest(
                        name = "updateRole",
                        description = "update description",
                        permissionGroupIds = listOf(1L, 2L),
                    )
                val permissionGroup1 = PermissionGroup(name = "Group 1", description = "Group 1 Description")
                val permissionGroup2 = PermissionGroup(name = "Group 2", description = "Group 2 Description")

                every { roleRepository.findWithInfoById(1L) } returns role
                every { rolePermissionRepository.deleteAllInBatch(any()) } just runs
                every { permissionGroupService.findPermissionGroupById(1L) } returns permissionGroup1
                every { permissionGroupService.findPermissionGroupById(2L) } returns permissionGroup2
                every { rolePermissionRepository.saveAll(any<List<RolePermission>>()) } returns listOf()

                Then("성공적으로 업데이트") {
                    roleService.update(1L, updateRequest)
                    role.name shouldBe updateRequest.name
                    role.description shouldBe updateRequest.description
                }
            }

            When("없는 Role ID로 업데이트 요청") {
                val updateRequest =
                    RoleUpdateRequest(
                        name = "New Name",
                        description = "New Description",
                        permissionGroupIds = null,
                    )

                every { roleRepository.findWithInfoById(999L) } returns null

                Then("CustomException 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        roleService.update(999L, updateRequest)
                    }.message shouldBe "ID가 999인 Role을 찾을 수 없습니다."
                }
            }
        }

        Given("Role 삭제를 진행할 때") {
            When("유효한 아이디로 삭제 요청") {
                val role = dummyRole(id = 1L, name = "Test Role", description = "Test Description")

                every { roleRepository.findWithInfoById(1L) } returns role
                every { rolePermissionRepository.deleteAllByRole(role) } just runs
                every { userRoleRepository.deleteAllByRole(role) } just runs
                every { em.flush() } just runs
                every { em.clear() } just runs
                every { roleRepository.deleteById(1L) } just runs

                Then("성공적으로 삭제") {
                    roleService.delete(1L)
                    verify(exactly = 1) { rolePermissionRepository.deleteAllByRole(role) }
                    verify(exactly = 1) { userRoleRepository.deleteAllByRole(role) }
                    verify(exactly = 1) { roleRepository.deleteById(1L) }
                }
            }

            When("없는 아이디로 삭제 요청") {
                every { roleRepository.findWithInfoById(999L) } returns null

                Then("CustomException 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        roleService.delete(999L)
                    }.message shouldBe "ID가 999인 Role을 찾을 수 없습니다."
                }
            }
        }

        Given("findRoleById 내부 메서드 호출 시") {
            When("유효한 ID로 호출") {
                val role = dummyRole(id = 1L, name = "Test Role", description = "Test Description")

                every { roleRepository.findWithInfoById(1L) } returns role

                Then("Role 객체 반환") {
                    val result = roleService.findRoleById(1L)
                    result shouldBe role
                }
            }

            When("없는 ID로 호출") {
                every { roleRepository.findWithInfoById(999L) } returns null

                Then("CustomException 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        roleService.findRoleById(999L)
                    }.message shouldBe "ID가 999인 Role을 찾을 수 없습니다."
                }
            }
        }
    })
