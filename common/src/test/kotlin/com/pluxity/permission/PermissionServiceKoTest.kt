package com.pluxity.permission

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.dto.PermissionCreateRequest
import com.pluxity.permission.dto.PermissionRequest
import com.pluxity.permission.dto.PermissionUpdateRequest
import com.pluxity.permission.entity.dummyPermission
import com.pluxity.user.repository.RolePermissionRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

class PermissionServiceKoTest :
    BehaviorSpec({

        val permissionRepository: PermissionRepository = mockk()
        val resourcePermissionRepository: ResourcePermissionRepository = mockk()
        val domainPermissionRepository: DomainPermissionRepository = mockk()
        val rolePermissionRepository: RolePermissionRepository = mockk()
        val permissionService =
            PermissionService(
                permissionRepository,
                resourcePermissionRepository,
                domainPermissionRepository,
                rolePermissionRepository,
            )

        Given("Permission 생성을 진행할 때") {
            When("중복된 이름으로 생성 요청") {
                val createRequest =
                    PermissionCreateRequest(
                        name = "Test Group",
                        description = "Test Description",
                        permissions =
                            listOf(
                                PermissionRequest("FACILITY", listOf("1", "2")),
                            ),
                    )

                every { permissionRepository.existsByName(any()) } returns true

                Then("DUPLICATE_PERMISSION_NAME 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionService.create(createRequest)
                    }.errorCode shouldBe ErrorCode.DUPLICATE_PERMISSION_NAME
                }
            }

            When("중복된 리소스 ID가 포함된 요청으로 생성") {
                val createRequest =
                    PermissionCreateRequest(
                        name = "Test Group",
                        description = "Test Description",
                        permissions =
                            listOf(
                                PermissionRequest("FACILITY", listOf("1", "1", "2")),
                            ),
                    )

                every { permissionRepository.existsByName(any()) } returns false

                Then("DUPLICATE_RESOURCE_ID 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionService.create(createRequest)
                    }.errorCode shouldBe ErrorCode.DUPLICATE_RESOURCE_ID
                }
            }

            When("유효한 요청으로 생성") {
                val createRequest =
                    PermissionCreateRequest(
                        name = "Test Group",
                        description = "Test Description",
                        permissions =
                            listOf(
                                PermissionRequest("FACILITY", listOf("1", "2")),
                            ),
                    )
                val savedGroup =
                    dummyPermission(
                        id = 1L,
                        name = createRequest.name,
                        description = createRequest.description,
                    )

                every { permissionRepository.existsByName(any()) } returns false
                every { permissionRepository.save(any()) } returns savedGroup

                Then("성공") {
                    val result = permissionService.create(createRequest)
                    result shouldBe 1L
                }
            }
        }

        Given("Permission 상세 조회를 진행할 때") {
            When("유효한 ID로 조회 요청") {
                val permission =
                    dummyPermission(
                        id = 1L,
                        name = "Test Group",
                        description = "Test Description",
                    )

                every { permissionRepository.findByIdOrNull(any()) } returns permission

                Then("성공") {
                    val result = permissionService.findById(1L)
                    result.id shouldBe 1L
                    result.name shouldBe "Test Group"
                    result.description shouldBe "Test Description"
                }
            }

            When("존재하지 않는 ID로 조회 요청") {
                every { permissionRepository.findByIdOrNull(any()) } returns null

                Then("NOT_FOUND_PERMISSION 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionService.findById(1L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_PERMISSION
                }
            }
        }

        Given("Permission 전체 목록 조회를 진행할 때") {
            When("정상 요청") {
                val permission =
                    dummyPermission(
                        id = 1L,
                        name = "Test Group",
                        description = "Test Description",
                    )

                every { permissionRepository.findAll() } returns listOf(permission)

                Then("성공") {
                    val result = permissionService.findAll()
                    result.size shouldBe 1
                    result.first().name shouldBe "Test Group"
                }
            }
        }

        Given("Permission 수정을 진행할 때") {
            When("이름 중복으로 수정 요청") {
                val existingGroup =
                    dummyPermission(
                        id = 1L,
                        name = "Old Name",
                        description = "Old Description",
                    )
                val updateRequest =
                    PermissionUpdateRequest(
                        name = "New Name",
                        description = "New Description",
                        permissions = listOf(),
                    )

                every { permissionRepository.findByIdOrNull(any()) } returns existingGroup
                every { permissionRepository.existsByNameAndIdNot(any(), any()) } returns true

                Then("DUPLICATE_PERMISSION_NAME 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionService.update(1L, updateRequest)
                    }.errorCode shouldBe ErrorCode.DUPLICATE_PERMISSION_NAME
                }
            }

            When("유효한 요청으로 수정") {
                val existingGroup =
                    dummyPermission(
                        id = 1L,
                        name = "Old Name",
                        description = "Old Description",
                    )
                val updateRequest =
                    PermissionUpdateRequest(
                        name = "New Name",
                        description = "New Description",
                        permissions =
                            listOf(
                                PermissionRequest("FACILITY", listOf("1", "2")),
                            ),
                    )

                every { permissionRepository.findByIdOrNull(any()) } returns existingGroup
                every { permissionRepository.existsByNameAndIdNot(any(), any()) } returns false
                every { resourcePermissionRepository.delete(any()) } just runs
                every { domainPermissionRepository.delete(any()) } just runs

                Then("성공") {
                    permissionService.update(1L, updateRequest)
                }
            }
        }

        Given("Permission 삭제를 진행할 때") {
            When("유효한 ID로 삭제 요청") {
                val permission =
                    dummyPermission(
                        id = 1L,
                        name = "Test Group",
                        description = "Test Description",
                    )

                every { permissionRepository.findByIdOrNull(any()) } returns permission
                every { rolePermissionRepository.deleteAllByPermission(any()) } just runs
                every { resourcePermissionRepository.deleteAll(any<Collection<ResourcePermission>>()) } just runs
                every { domainPermissionRepository.deleteAll(any<Collection<DomainPermission>>()) } just runs
                every { permissionRepository.delete(any()) } just runs

                Then("성공") {
                    permissionService.delete(1L)

                    verify(exactly = 1) { rolePermissionRepository.deleteAllByPermission(permission) }
                    verify(exactly = 1) { resourcePermissionRepository.deleteAll(permission.resourcePermissions) }
                    verify(exactly = 1) { domainPermissionRepository.deleteAll(permission.domainPermissions) }
                    verify(exactly = 1) { permissionRepository.delete(permission) }
                }
            }

            When("존재하지 않는 ID로 삭제 요청") {
                every { permissionRepository.findByIdOrNull(any()) } returns null

                Then("NOT_FOUND_PERMISSION 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionService.delete(1L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_PERMISSION
                }
            }
        }

        Given("findPermissionById를 진행할 때") {
            When("유효한 ID로 조회 요청") {
                val permission =
                    dummyPermission(
                        id = 1L,
                        name = "Test Group",
                        description = "Test Description",
                    )

                every { permissionRepository.findByIdOrNull(any()) } returns permission

                Then("성공") {
                    val result = permissionService.findPermissionById(1L)
                    result.id shouldBe 1L
                    result.name shouldBe "Test Group"
                }
            }

            When("존재하지 않는 ID로 조회 요청") {
                every { permissionRepository.findByIdOrNull(any()) } returns null

                Then("NOT_FOUND_PERMISSION 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        permissionService.findPermissionById(1L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_PERMISSION
                }
            }
        }
    })
