package com.pluxity.user.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.Permission
import com.pluxity.permission.PermissionService
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleUpdateRequest
import com.pluxity.user.entity.RolePermission
import com.pluxity.user.entity.dummyRole
import com.pluxity.user.repository.RolePermissionRepository
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRoleRepository
import io.kotest.assertions.throwables.shouldThrowExactly
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
        val roleRepository: RoleRepository = mockk()
        val rolePermissionRepository: RolePermissionRepository = mockk()
        val userRoleRepository: UserRoleRepository = mockk()
        val permissionService: PermissionService = mockk()
        val em: EntityManager = mockk()

        val roleService =
            RoleService(
                roleRepository,
                rolePermissionRepository,
                userRoleRepository,
                permissionService,
                em,
            )

        Given("Role 생성을 진행할 때") {
            When("유효한 요청으로 Role 생성 요청") {
                val createRequest =
                    RoleCreateRequest(
                        name = "Test Role",
                        description = "Test Description",
                        permissionIds = listOf(1L, 2L),
                    )
                val savedRole = dummyRole(name = createRequest.name, description = createRequest.description)
                val permission1 =
                    Permission(
                        name = "Group 1",
                        description = "Group 1 Description",
                    )
                val permission2 =
                    Permission(
                        name = "Group 2",
                        description = "Group 2 Description",
                    )

                every { roleRepository.save(any()) } returns savedRole
                every { permissionService.findPermissionById(1L) } returns permission1
                every { permissionService.findPermissionById(2L) } returns permission2
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
                        permissionIds = emptyList(),
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
                val role1 = dummyRole(name = "Role1", description = "Desc1")
                val role2 = dummyRole(id = 2L, name = "Role2", description = "Desc2")

                every { roleRepository.findAllByOrderByCreatedAtDesc() } returns listOf(role1, role2)

                Then("정상 조회") {
                    val result = roleService.findAll()
                    result.size shouldBe 2
                    result[0].name shouldBe "Role1"
                    result[1].name shouldBe "Role2"
                }
            }

            When("데이터가 없을 때") {
                every { roleRepository.findAllByOrderByCreatedAtDesc() } returns emptyList()

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
                    }.message shouldBe ErrorCode.NOT_FOUND_ROLE.getMessage().format(999L)
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
                        permissionIds = null,
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
                        permissionIds = listOf(1L, 2L),
                    )
                val permission1 = Permission(name = "Group 1", description = "Group 1 Description")
                val permission2 = Permission(name = "Group 2", description = "Group 2 Description")

                every { roleRepository.findWithInfoById(1L) } returns role
                every { rolePermissionRepository.deleteAllInBatch(any()) } just runs
                every { permissionService.findPermissionById(1L) } returns permission1
                every { permissionService.findPermissionById(2L) } returns permission2
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
                        permissionIds = null,
                    )

                every { roleRepository.findWithInfoById(999L) } returns null

                Then("CustomException 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        roleService.update(999L, updateRequest)
                    }.message shouldBe ErrorCode.NOT_FOUND_ROLE.getMessage().format(999L)
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
                    }.message shouldBe ErrorCode.NOT_FOUND_ROLE.getMessage().format(999L)
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
                    }.message shouldBe ErrorCode.NOT_FOUND_ROLE.getMessage().format(999L)
                }
            }
        }
    })
