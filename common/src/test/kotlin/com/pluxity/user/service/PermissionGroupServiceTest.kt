package com.pluxity.user.service

import com.pluxity.config.MockBeansConfig
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionGroupRepository
import com.pluxity.permission.PermissionGroupService
import com.pluxity.permission.PermissionRepository
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionGroupUpdateRequest
import com.pluxity.permission.dto.PermissionRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class PermissionGroupServiceTest
    @Autowired
    constructor(
        private val permissionGroupService: PermissionGroupService,
        private val permissionGroupRepository: PermissionGroupRepository,
        private val permissionRepository: PermissionRepository,
    ) {
        private lateinit var createRequest: PermissionGroupCreateRequest

        @BeforeEach
        fun setUp() {
            // 여러 테스트에서 사용할 기본 생성 요청 DTO
            createRequest =
                PermissionGroupCreateRequest(
                    name = "기본 시설 관리 그룹",
                    description = "시설에 대한 기본 권한",
                    permissions =
                        listOf(
                            PermissionRequest(ResourceType.FACILITY.name, listOf("READ", "LIST")),
                            PermissionRequest(ResourceType.DEVICE_CATEGORY.name, listOf("READ")),
                        ),
                )
        }

        @Nested
        @DisplayName("권한 그룹 생성 (Create)")
        internal inner class CreatePermissionGroup {
            @Test
            @DisplayName("성공: 유효한 요청으로 권한 그룹 생성 시, 그룹과 모든 하위 권한들이 올바르게 저장된다")
            fun withValidRequest_shouldSaveGroupAndAllPermissions() {
                // when
                val groupId = permissionGroupService.create(createRequest)

                // then
                assertThat(groupId).isNotNull()

                // 저장된 그룹 확인
                val foundGroup = permissionGroupRepository.findById(groupId).orElseThrow()
                assertThat(foundGroup.name).isEqualTo("기본 시설 관리 그룹")
                assertThat(foundGroup.description).isEqualTo("시설에 대한 기본 권한")

                // 저장된 권한 확인
                val permissions = foundGroup.permissions
                assertThat(permissions).hasSize(3)

                // FACILITY 권한 검증
                val facilityPermissions =
                    permissions
                        .filter { it.resourceName == "FACILITY" }
                        .map { it.resourceId }
                assertThat(facilityPermissions).containsExactlyInAnyOrder("READ", "LIST")

                // DEVICE_CATEGORY 권한 검증
                val deviceCategoryPermissions =
                    permissions
                        .filter { it.resourceName == "DEVICE_CATEGORY" }
                        .map { it.resourceId }
                assertThat(deviceCategoryPermissions).containsExactly("READ")
            }

            @Test
            @DisplayName("실패: 중복된 그룹 이름으로 생성 시도 시 DUPLICATE_PERMISSION_GROUP_NAME 예외가 발생한다")
            fun withDuplicateGroupName_shouldThrowException() {
                // given
                permissionGroupService.create(createRequest) // 먼저 하나 생성
                val duplicateRequest =
                    PermissionGroupCreateRequest(
                        "기본 시설 관리 그룹", // 중복된 이름
                        "다른 설명",
                        listOf(PermissionRequest("PARK", listOf("VIEW"))),
                    )

                // when and then
                val exception =
                    assertThrows<CustomException> {
                        permissionGroupService.create(duplicateRequest)
                    }
                assertThat(exception.errorCode).isEqualTo(ErrorCode.DUPLICATE_PERMISSION_GROUP_NAME)
            }

            @Test
            @DisplayName("실패: 요청 DTO의 한 권한 목록 내에 중복된 리소스 ID가 포함된 경우 DUPLICATE_RESOURCE_ID 예외가 발생한다")
            fun withDuplicateResourceIdsInRequest_shouldThrowException() {
                // given
                val duplicateRequest =
                    PermissionGroupCreateRequest(
                        "잘못된 그룹",
                        "설명",
                        listOf(
                            PermissionRequest(
                                "FACILITY",
                                listOf("READ", "LIST", "READ"), // 중복
                            ),
                        ),
                    )

                // when and then
                val exception =
                    assertThrows<CustomException> {
                        permissionGroupService.create(duplicateRequest)
                    }
                assertThat(exception.errorCode).isEqualTo(ErrorCode.DUPLICATE_RESOURCE_ID)
            }
        }

        @Nested
        @DisplayName("권한 그룹 조회 (Read)")
        internal inner class ReadPermissionGroup {
            private var groupId: Long? = null

            @BeforeEach
            fun setUp() {
                groupId = permissionGroupService.create(createRequest)
            }

            @Test
            @DisplayName("성공: 존재하는 ID로 조회 시 그룹 정보와 하위 권한들이 DTO로 반환된다")
            fun findById_withExistingId_shouldReturnResponse() {
                // when
                val response = permissionGroupService.findById(groupId!!)

                // then
                assertThat(response).isNotNull()
                assertThat(response.id).isEqualTo(groupId)
                assertThat(response.description).isEqualTo("시설에 대한 기본 권한")
                assertThat(response.name).isEqualTo("기본 시설 관리 그룹")
                assertThat(response.permissions).hasSize(2)
                assertThat(response.permissions.first().resourceType).isNotNull().isNotEmpty()
                assertThat(
                    response.permissions
                        .first()
                        .resourceType.javaClass,
                ).isEqualTo(String::class.java)
                assertThat(response.permissions.first().resourceIds).isNotNull().isNotEmpty()
            }

            @Test
            @DisplayName("실패: 존재하지 않는 ID로 조회 시 NOT_FOUND_PERMISSION_GROUP 예외가 발생한다")
            fun findById_withNonExistingId_shouldThrowException() {
                // given
                val nonExistingId = 9999L

                // when and then
                val exception =
                    assertThrows<CustomException> {
                        permissionGroupService.findById(nonExistingId)
                    }
                assertThat(exception.errorCode).isEqualTo(ErrorCode.NOT_FOUND_PERMISSION_GROUP)
            }
        }

        @Nested
        @DisplayName("권한 그룹 수정 (Update - PATCH 방식)")
        internal inner class UpdatePermissionGroup {
            private var groupId: Long? = null

            @BeforeEach
            fun setUp() {
                // 수정된 ResourceType과 일치하는 테스트 데이터 생성
                val createRequest =
                    PermissionGroupCreateRequest(
                        "기본 시설 관리 그룹",
                        "시설 및 장비 분류에 대한 기본 권한",
                        listOf( // name()을 사용하여 "시설" 문자열을 전달
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                listOf("READ", "LIST"),
                            ), // name()을 사용하여 "장비 분류" 문자열을 전달
                            PermissionRequest(ResourceType.DEVICE_CATEGORY.name, listOf("READ")),
                        ),
                    )
                groupId = permissionGroupService.create(createRequest)
            }

            @Test
            @DisplayName("성공: 권한을 추가, 유지, 삭제하는 복합적인 수정이 정상적으로 반영된다")
            fun withValidRequest_shouldPatchPermissionsCorrectly() {
                // given
                // 기존 상태: FACILITY(READ, LIST), DEVICE_CATEGORY(READ)
                // 목표 상태: FACILITY(EDIT, LIST), DEVICE_CATEGORY(CREATE)
                // 변경 내역: FACILITY(READ) 삭제, DEVICE_CATEGORY(READ) 삭제, FACILITY(EDIT) 추가,
                // DEVICE_CATEGORY(CREATE) 추가
                val updateRequest =
                    PermissionGroupUpdateRequest(
                        "수정된 고급 그룹",
                        "수정된 설명입니다.",
                        listOf( // "시설" 문자열로 요청
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                listOf("EDIT", "LIST"),
                            ), // "장비 분류" 문자열로 요청
                            PermissionRequest(ResourceType.DEVICE_CATEGORY.name, listOf("CREATE")),
                        ),
                    )

                // when
                permissionGroupService.update(groupId!!, updateRequest)

                // then
                val updatedGroup = permissionGroupRepository.findById(groupId!!).orElseThrow()
                assertThat(updatedGroup.name).isEqualTo("수정된 고급 그룹")
                assertThat(updatedGroup.description).isEqualTo("수정된 설명입니다.")

                val permissions = updatedGroup.permissions
                assertThat(permissions)
                    .hasSize(3) // 최종 3개 (FACILITY:EDIT, FACILITY:LIST, DEVICE_CATEGORY:CREATE)

                val permissionKeys =
                    permissions
                        .map { "${it.resourceName}:${it.resourceId}" }
                        .toSet()

                // 최종 상태 검증
                assertThat(permissionKeys)
                    .containsExactlyInAnyOrder(
                        "FACILITY:EDIT", // 추가됨
                        "FACILITY:LIST", // 유지됨
                        "DEVICE_CATEGORY:CREATE", // 추가됨
                    )

                // 삭제된 권한 검증
                assertThat(permissionKeys)
                    .doesNotContain(
                        "FACILITY:READ", // 삭제됨
                        "DEVICE_CATEGORY:READ", // 삭제됨
                    )
            }

            @Test
            @DisplayName("실패: 다른 그룹과 중복되는 이름으로 수정 시도 시 예외가 발생한다")
            fun withDuplicateGroupName_shouldThrowException() {
                // given
                // 비교 대상 그룹 생성
                val anotherRequest =
                    PermissionGroupCreateRequest(
                        "다른 그룹",
                        "다른 설명",
                        listOf(
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                listOf("P1"),
                            ),
                        ),
                    )
                permissionGroupService.create(anotherRequest)

                // 기존 그룹을 '다른 그룹'과 동일한 이름으로 업데이트 시도
                val updateRequest =
                    PermissionGroupUpdateRequest(
                        "다른 그룹", // 중복되는 이름
                        "설명",
                        emptyList(),
                    )

                // when and then
                assertThrows<CustomException> {
                    permissionGroupService.update(groupId!!, updateRequest)
                }
            }
        }

        @Nested
        @DisplayName("권한 그룹 삭제 (Delete)")
        internal inner class DeletePermissionGroup {
            private var groupId: Long? = null

            @BeforeEach
            fun setUp() {
                groupId = permissionGroupService.create(createRequest)
            }

            @Test
            @DisplayName("성공: 존재하는 ID의 그룹 삭제 시, 그룹과 하위 권한들이 모두 삭제된다")
            fun withExistingId_shouldDeleteGroupAndPermissions() {
                // given
                val initialPermissionCount = permissionRepository.count()
                assertThat(permissionGroupRepository.existsById(groupId!!)).isTrue()

                // when
                permissionGroupService.delete(groupId!!)

                // then
                assertThat(permissionGroupRepository.existsById(groupId!!)).isFalse()
                // 그룹에 속해있던 3개의 권한이 삭제되었는지 확인
                assertThat(permissionRepository.count()).isEqualTo(initialPermissionCount - 3)
            }
        }

        @Test
        @DisplayName("성공: 빈 권한 목록으로 권한 그룹을 생성할 수 있다")
        fun create_withEmptyPermissions_shouldSucceed() {
            // GIVEN
            val request =
                PermissionGroupCreateRequest(
                    "권한 없는 그룹",
                    "설명",
                    emptyList(), // 빈 리스트
                )

            // WHEN
            val groupId = permissionGroupService.create(request)

            // THEN
            val foundGroup = permissionGroupRepository.findById(groupId).orElseThrow()
            assertThat(foundGroup.name).isEqualTo("권한 없는 그룹")
            assertThat(foundGroup.permissions).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공: 모든 권한 그룹 조회 시 전체 목록이 반환된다")
        fun findAll_shouldReturnAllPermissionGroups() {
            // GIVEN
            permissionGroupService.create(createRequest)
            permissionGroupService.create(PermissionGroupCreateRequest("추가 그룹", null, emptyList()))

            // WHEN
            val responses = permissionGroupService.findAll()

            // THEN
            assertThat(responses).hasSize(2)
        }

        @Test
        @DisplayName("성공: 권한 그룹이 없을 때 전체 조회 시 빈 리스트가 반환된다")
        fun findAll_whenNoGroupsExist_shouldReturnEmptyList() {
            // GIVEN: 데이터가 없는 상태

            // WHEN
            val responses = permissionGroupService.findAll()

            // THEN
            assertThat(responses).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공: 권한을 빈 리스트로 업데이트하여 모든 권한을 제거할 수 있다")
        fun update_withEmptyPermissionList_shouldRemoveAllPermissions() {
            // GIVEN
            val groupId = permissionGroupService.create(createRequest)
            assertThat(permissionGroupRepository.findById(groupId).orElseThrow().permissions)
                .isNotEmpty()

            val updateRequest =
                PermissionGroupUpdateRequest(
                    "권한 제거된 그룹",
                    null,
                    emptyList(), // 빈 리스트로 업데이트
                )

            // WHEN
            permissionGroupService.update(groupId, updateRequest)

            // THEN
            val updatedGroup = permissionGroupRepository.findById(groupId).orElseThrow()
            assertThat(updatedGroup.name).isEqualTo("권한 제거된 그룹")
            assertThat(updatedGroup.permissions).isEmpty()
        }

        @Test
        @DisplayName("성공: 권한 목록 변경 없이 이름과 설명만 수정할 수 있다")
        fun update_onlyNameAndDescription_shouldSucceed() {
            // GIVEN
            val groupId = permissionGroupService.create(createRequest)
            val permissionGroup = permissionGroupService.findPermissionGroupById(groupId)

            val permissionRequests =
                permissionGroup.permissions.map { permission ->
                    PermissionRequest(permission.resourceName, listOf(permission.resourceId))
                }
            val updateRequest = PermissionGroupUpdateRequest("이름만 변경", "설명만 변경", permissionRequests)

            // WHEN
            permissionGroupService.update(groupId, updateRequest)

            // THEN
            val updatedGroup = permissionGroupRepository.findById(groupId).orElseThrow()
            assertThat(updatedGroup.name).isEqualTo("이름만 변경")
            assertThat(updatedGroup.description).isEqualTo("설명만 변경")
            assertThat(updatedGroup.permissions).hasSize(3)
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 업데이트 시도 시 예외가 발생한다")
        fun update_withNonExistingId_shouldThrowException() {
            // GIVEN
            val request = PermissionGroupUpdateRequest("이름", "설명", emptyList())

            // WHEN & THEN
            assertThrows<CustomException> {
                permissionGroupService.update(9999L, request)
            }
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 삭제 시도 시 예외가 발생한다")
        fun delete_withNonExistingId_shouldThrowException() {
            // WHEN & THEN
            assertThrows<CustomException> {
                permissionGroupService.delete(9999L)
            }
        }
    }
