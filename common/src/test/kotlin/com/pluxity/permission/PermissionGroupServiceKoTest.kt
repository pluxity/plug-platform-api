package com.pluxity.permission

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionGroupUpdateRequest
import com.pluxity.permission.dto.PermissionRequest
import com.pluxity.permission.entity.dummyPermissionGroup
import com.pluxity.user.repository.RolePermissionRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

class PermissionGroupServiceKoTest :
    BehaviorSpec({
        isolationMode = IsolationMode.InstancePerLeaf

        val permissionGroupRepository: PermissionGroupRepository = mockk()
        val permissionRepository: PermissionRepository = mockk()
        val rolePermissionRepository: RolePermissionRepository = mockk()
        val permissionGroupService =
            PermissionGroupService(
                permissionGroupRepository,
                permissionRepository,
                rolePermissionRepository,
            )

        Given("PermissionGroup 생성을 진행할 때") {
            When("중복된 이름으로 생성 요청") {
                val createRequest =
                    PermissionGroupCreateRequest(
                        name = "Test Group",
                        description = "Test Description",
                        permissions =
                            listOf(
                                PermissionRequest("FACILITY", listOf("1", "2")),
                            ),
                    )

                every { permissionGroupRepository.existsByName(any()) } returns true

                Then("DUPLICATE_PERMISSION_GROUP_NAME 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionGroupService.create(createRequest)
                    }.errorCode shouldBe ErrorCode.DUPLICATE_PERMISSION_GROUP_NAME
                }
            }

            When("중복된 리소스 ID가 포함된 요청으로 생성") {
                val createRequest =
                    PermissionGroupCreateRequest(
                        name = "Test Group",
                        description = "Test Description",
                        permissions =
                            listOf(
                                PermissionRequest("FACILITY", listOf("1", "1", "2")),
                            ),
                    )

                every { permissionGroupRepository.existsByName(any()) } returns false

                Then("DUPLICATE_RESOURCE_ID 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionGroupService.create(createRequest)
                    }.errorCode shouldBe ErrorCode.DUPLICATE_RESOURCE_ID
                }
            }

            When("유효한 요청으로 생성") {
                val createRequest =
                    PermissionGroupCreateRequest(
                        name = "Test Group",
                        description = "Test Description",
                        permissions =
                            listOf(
                                PermissionRequest("FACILITY", listOf("1", "2")),
                            ),
                    )
                val savedGroup =
                    dummyPermissionGroup(
                        id = 1L,
                        name = createRequest.name,
                        description = createRequest.description,
                    )

                every { permissionGroupRepository.existsByName(any()) } returns false
                every { permissionGroupRepository.save(any()) } returns savedGroup

                Then("성공") {
                    val result = permissionGroupService.create(createRequest)
                    result shouldBe 1L
                }
            }
        }

        Given("PermissionGroup 상세 조회를 진행할 때") {
            When("유효한 ID로 조회 요청") {
                val permissionGroup =
                    dummyPermissionGroup(
                        id = 1L,
                        name = "Test Group",
                        description = "Test Description",
                    )

                every { permissionGroupRepository.findByIdOrNull(any()) } returns permissionGroup

                Then("성공") {
                    val result = permissionGroupService.findById(1L)
                    result.id shouldBe 1L
                    result.name shouldBe "Test Group"
                    result.description shouldBe "Test Description"
                }
            }

            When("존재하지 않는 ID로 조회 요청") {
                every { permissionGroupRepository.findByIdOrNull(any()) } returns null

                Then("NOT_FOUND_PERMISSION_GROUP 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionGroupService.findById(1L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_PERMISSION_GROUP
                }
            }
        }

        Given("PermissionGroup 전체 목록 조회를 진행할 때") {
            When("정상 요청") {
                val permissionGroup =
                    dummyPermissionGroup(
                        id = 1L,
                        name = "Test Group",
                        description = "Test Description",
                    )

                every { permissionGroupRepository.findAll() } returns listOf(permissionGroup)

                Then("성공") {
                    val result = permissionGroupService.findAll()
                    result.size shouldBe 1
                    result.first().name shouldBe "Test Group"
                }
            }
        }

        Given("PermissionGroup 수정을 진행할 때") {
            When("이름 중복으로 수정 요청") {
                val existingGroup =
                    dummyPermissionGroup(
                        id = 1L,
                        name = "Old Name",
                        description = "Old Description",
                    )
                val updateRequest =
                    PermissionGroupUpdateRequest(
                        name = "New Name",
                        description = "New Description",
                        permissions = listOf(),
                    )

                every { permissionGroupRepository.findByIdOrNull(any()) } returns existingGroup
                every { permissionGroupRepository.existsByNameAndIdNot(any(), any()) } returns true

                Then("DUPLICATE_PERMISSION_GROUP_NAME 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionGroupService.update(1L, updateRequest)
                    }.errorCode shouldBe ErrorCode.DUPLICATE_PERMISSION_GROUP_NAME
                }
            }

            When("유효한 요청으로 수정") {
                val existingGroup =
                    dummyPermissionGroup(
                        id = 1L,
                        name = "Old Name",
                        description = "Old Description",
                    )
                val updateRequest =
                    PermissionGroupUpdateRequest(
                        name = "New Name",
                        description = "New Description",
                        permissions =
                            listOf(
                                PermissionRequest("FACILITY", listOf("1", "2")),
                            ),
                    )

                every { permissionGroupRepository.findByIdOrNull(any()) } returns existingGroup
                every { permissionGroupRepository.existsByNameAndIdNot(any(), any()) } returns false
                every { permissionRepository.delete(any()) } just runs

                Then("성공") {
                    permissionGroupService.update(1L, updateRequest)
                }
            }
        }

        Given("PermissionGroup 삭제를 진행할 때") {
            When("유효한 ID로 삭제 요청") {
                val permissionGroup =
                    dummyPermissionGroup(
                        id = 1L,
                        name = "Test Group",
                        description = "Test Description",
                    )

                every { permissionGroupRepository.findByIdOrNull(any()) } returns permissionGroup
                every { rolePermissionRepository.deleteAllByPermissionGroup(any()) } just runs
                every { permissionRepository.deleteAll(any<Collection<Permission>>()) } just runs
                every { permissionGroupRepository.delete(any()) } just runs

                Then("성공") {
                    permissionGroupService.delete(1L)

                    verify(exactly = 1) { rolePermissionRepository.deleteAllByPermissionGroup(permissionGroup) }
                    verify(exactly = 1) { permissionRepository.deleteAll(permissionGroup.permissions) }
                    verify(exactly = 1) { permissionGroupRepository.delete(permissionGroup) }
                }
            }

            When("존재하지 않는 ID로 삭제 요청") {
                every { permissionGroupRepository.findByIdOrNull(any()) } returns null

                Then("NOT_FOUND_PERMISSION_GROUP 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionGroupService.delete(1L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_PERMISSION_GROUP
                }
            }
        }

        Given("findPermissionGroupById를 진행할 때") {
            When("유효한 ID로 조회 요청") {
                val permissionGroup =
                    dummyPermissionGroup(
                        id = 1L,
                        name = "Test Group",
                        description = "Test Description",
                    )

                every { permissionGroupRepository.findByIdOrNull(any()) } returns permissionGroup

                Then("성공") {
                    val result = permissionGroupService.findPermissionGroupById(1L)
                    result.id shouldBe 1L
                    result.name shouldBe "Test Group"
                }
            }

            When("존재하지 않는 ID로 조회 요청") {
                every { permissionGroupRepository.findByIdOrNull(any()) } returns null

                Then("NOT_FOUND_PERMISSION_GROUP 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionGroupService.findPermissionGroupById(1L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_PERMISSION_GROUP
                }
            }
        }
    })
