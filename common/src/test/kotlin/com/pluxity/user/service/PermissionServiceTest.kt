package com.pluxity.user.service

import com.pluxity.config.MockBeansConfig
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.PermissionRepository
import com.pluxity.permission.PermissionService
import com.pluxity.permission.ResourcePermissionRepository
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.PermissionCreateRequest
import com.pluxity.permission.dto.PermissionRequest
import com.pluxity.permission.dto.PermissionUpdateRequest
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
import kotlin.properties.Delegates

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class PermissionServiceTest
    @Autowired
    constructor(
        private val permissionService: PermissionService,
        private val permissionRepository: PermissionRepository,
        private val resourcePermissionRepository: ResourcePermissionRepository,
    ) {
        private lateinit var createRequest: PermissionCreateRequest

        @BeforeEach
        fun setUp() {
            // 여러 테스트에서 사용할 기본 생성 요청 DTO
            createRequest =
                PermissionCreateRequest(
                    name = "기본 시설 관리 그룹",
                    description = "시설에 대한 기본 권한",
                    permissions =
                        listOf(
                            PermissionRequest(ResourceType.FACILITY.name, listOf("f1", "f2"), PermissionLevel.READ),
                            PermissionRequest(ResourceType.CCTV.name, listOf("c1"), PermissionLevel.READ),
                        ),
                )
        }

        @Nested
        @DisplayName("권한 생성 (Create)")
        internal inner class CreatePermission {
            @Test
            @DisplayName("성공: 유효한 요청으로 권한 생성 시, 권한과 모든 하위 권한들이 올바르게 저장된다")
            fun withValidRequest_shouldSaveGroupAndAllPermissions() {
                // when
                val groupId = permissionService.create(createRequest)

                // then
                assertThat(groupId).isNotNull()

                // 저장된 그룹 확인
                val foundGroup = permissionRepository.findById(groupId).orElseThrow()
                assertThat(foundGroup.name).isEqualTo("기본 시설 관리 그룹")
                assertThat(foundGroup.description).isEqualTo("시설에 대한 기본 권한")

                // 저장된 권한 확인
                val permissions = foundGroup.resourcePermissions
                assertThat(permissions).hasSize(3)

                // FACILITY 권한 검증
                val facilityPermissions =
                    permissions
                        .filter { it.resourceName == "FACILITY" }
                        .map { it.resourceId }
                assertThat(facilityPermissions).containsExactlyInAnyOrder("f1", "f2")

                // CCTV 권한 검증
                val cctvPermissions =
                    permissions
                        .filter { it.resourceName == "CCTV" }
                        .map { it.resourceId }
                assertThat(cctvPermissions).containsExactly("c1")
            }

            @Test
            @DisplayName("실패: 중복된 이름으로 생성 시도 시 DUPLICATE_PERMISSION_NAME 예외가 발생한다")
            fun withDuplicateGroupName_shouldThrowException() {
                // given
                permissionService.create(createRequest) // 먼저 하나 생성
                val duplicateRequest =
                    PermissionCreateRequest(
                        "기본 시설 관리 그룹", // 중복된 이름
                        "다른 설명",
                        listOf(PermissionRequest("PARK", listOf("VIEW"))),
                    )

                // when and then
                val exception =
                    assertThrows<CustomException> {
                        permissionService.create(duplicateRequest)
                    }
                assertThat(exception.errorCode).isEqualTo(ErrorCode.DUPLICATE_PERMISSION_NAME)
            }

            @Test
            @DisplayName("실패: 요청 DTO의 한 권한 목록 내에 중복된 리소스 ID가 포함된 경우 DUPLICATE_RESOURCE_ID 예외가 발생한다")
            fun withDuplicateResourceIdsInRequest_shouldThrowException() {
                // given
                val duplicateRequest =
                    PermissionCreateRequest(
                        "잘못된 그룹",
                        "설명",
                        listOf(
                            PermissionRequest(
                                "FACILITY",
                                listOf("f1", "f1"), // 중복
                                PermissionLevel.READ,
                            ),
                        ),
                    )

                // when and then
                val exception =
                    assertThrows<CustomException> {
                        permissionService.create(duplicateRequest)
                    }
                assertThat(exception.errorCode).isEqualTo(ErrorCode.DUPLICATE_RESOURCE_ID)
            }
        }

        @Nested
        @DisplayName("권한 조회 (Read)")
        internal inner class ReadPermission {
            private var groupId: Long by Delegates.notNull()

            @BeforeEach
            fun setUp() {
                groupId = permissionService.create(createRequest)
            }

            @Test
            @DisplayName("성공: 존재하는 ID로 조회 시 권한 정보와 하위 권한들이 DTO로 반환된다")
            fun findById_withExistingId_shouldReturnResponse() {
                // when
                val response = permissionService.findById(groupId)

                // then
                assertThat(response).isNotNull()
                assertThat(response.id).isEqualTo(groupId)
                assertThat(response.description).isEqualTo("시설에 대한 기본 권한")
                assertThat(response.name).isEqualTo("기본 시설 관리 그룹")
                assertThat(response.resourcePermissions).hasSize(2)
                assertThat(response.resourcePermissions.first().resourceType).isNotNull().isNotEmpty()
                assertThat(
                    response.resourcePermissions
                        .first()
                        .resourceType.javaClass,
                ).isEqualTo(String::class.java)
                assertThat(response.resourcePermissions.first().permissions).isNotNull().isNotEmpty()
                assertThat(response.domainPermissions).isEmpty()
            }

            @Test
            @DisplayName("실패: 존재하지 않는 ID로 조회 시 NOT_FOUND_PERMISSION 예외가 발생한다")
            fun findById_withNonExistingId_shouldThrowException() {
                // given
                val nonExistingId = 9999L

                // when and then
                val exception =
                    assertThrows<CustomException> {
                        permissionService.findById(nonExistingId)
                    }
                assertThat(exception.errorCode).isEqualTo(ErrorCode.NOT_FOUND_PERMISSION)
            }
        }

        @Nested
        @DisplayName("권한 수정 (Update - PATCH 방식)")
        internal inner class UpdatePermission {
            private var groupId: Long by Delegates.notNull()

            @BeforeEach
            fun setUp() {
                // 수정된 ResourceType과 일치하는 테스트 데이터 생성
                val createRequest =
                    PermissionCreateRequest(
                        "기본 시설 관리 그룹",
                        "시설 및 CCTV에 대한 기본 권한",
                        listOf( // name()을 사용하여 "시설" 문자열을 전달
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                listOf("f1", "f2"),
                                PermissionLevel.READ,
                            ), // name()을 사용하여 "CCTV" 문자열을 전달
                            PermissionRequest(ResourceType.CCTV.name, listOf("c1"), PermissionLevel.READ),
                        ),
                    )
                groupId = permissionService.create(createRequest)
            }

            @Test
            @DisplayName("성공: 권한을 추가, 유지, 삭제하는 복합적인 수정이 정상적으로 반영된다")
            fun withValidRequest_shouldPatchPermissionsCorrectly() {
                // given
                // 기존 상태: FACILITY(READ, LIST), CCTV(READ)
                // 목표 상태: FACILITY(EDIT, LIST), CCTV(CREATE)
                // 변경 내역: FACILITY(READ) 삭제, CCTV(READ) 삭제, FACILITY(EDIT) 추가,
                // CCTV(CREATE) 추가
                val updateRequest =
                    PermissionUpdateRequest(
                        "수정된 고급 그룹",
                        "수정된 설명입니다.",
                        listOf( // "시설" 문자열로 요청
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                listOf("f2", "f3"),
                                PermissionLevel.WRITE,
                            ), // "CCTV" 문자열로 요청
                            PermissionRequest(ResourceType.CCTV.name, listOf("c2"), PermissionLevel.ADMIN),
                        ),
                    )

                // when
                permissionService.update(groupId, updateRequest)

                // then
                val updatedGroup = permissionRepository.findById(groupId).orElseThrow()
                assertThat(updatedGroup.name).isEqualTo("수정된 고급 그룹")
                assertThat(updatedGroup.description).isEqualTo("수정된 설명입니다.")

                val permissions = updatedGroup.resourcePermissions
                assertThat(permissions)
                    .hasSize(3) // 최종 3개 (FACILITY:EDIT, FACILITY:LIST, CCTV:CREATE)

                val permissionKeys =
                    permissions
                        .map { "${it.resourceName}:${it.resourceId}" }
                        .toSet()

                // 최종 상태 검증
                assertThat(permissionKeys)
                    .containsExactlyInAnyOrder(
                        "FACILITY:f2", // 유지됨
                        "FACILITY:f3", // 추가됨
                        "CCTV:c2", // 추가됨
                    )

                // 삭제된 권한 검증
                assertThat(permissionKeys)
                    .doesNotContain(
                        "FACILITY:f1", // 삭제됨
                        "CCTV:c1", // 삭제됨
                    )

                val updatedFacilityPermissions =
                    updatedGroup.resourcePermissions.filter { it.resourceName == "FACILITY" }
                assertThat(updatedFacilityPermissions.map { it.level }.distinct())
                    .containsExactly(PermissionLevel.WRITE)
            }

            @Test
            @DisplayName("실패: 다른 그룹과 중복되는 이름으로 수정 시도 시 예외가 발생한다")
            fun withDuplicateGroupName_shouldThrowException() {
                // given
                // 비교 대상 그룹 생성
                val anotherRequest =
                    PermissionCreateRequest(
                        "다른 그룹",
                        "다른 설명",
                        listOf(
                            PermissionRequest(
                                ResourceType.FACILITY.name,
                                listOf("f1"),
                                PermissionLevel.READ,
                            ),
                        ),
                    )
                permissionService.create(anotherRequest)

                // 기존 그룹을 '다른 그룹'과 동일한 이름으로 업데이트 시도
                val updateRequest =
                    PermissionUpdateRequest(
                        "다른 그룹", // 중복되는 이름
                        "설명",
                        emptyList(),
                    )

                // when and then
                assertThrows<CustomException> {
                    permissionService.update(groupId, updateRequest)
                }
            }
        }

        @Nested
        @DisplayName("권한 삭제 (Delete)")
        internal inner class DeletePermission {
            private var groupId: Long by Delegates.notNull()

            @BeforeEach
            fun setUp() {
                groupId = permissionService.create(createRequest)
            }

            @Test
            @DisplayName("성공: 존재하는 ID의 그룹 삭제 시, 그룹과 하위 권한들이 모두 삭제된다")
            fun withExistingId_shouldDeleteGroupAndPermissions() {
                // given
                val initialPermissionCount = resourcePermissionRepository.count()
                assertThat(permissionRepository.existsById(groupId)).isTrue()

                // when
                permissionService.delete(groupId)

                // then
                assertThat(permissionRepository.existsById(groupId)).isFalse()
                // 그룹에 속해있던 3개의 권한이 삭제되었는지 확인
                assertThat(resourcePermissionRepository.count()).isEqualTo(initialPermissionCount - 3)
            }
        }

        @Test
        @DisplayName("성공: 빈 권한 목록으로 권한 그룹을 생성할 수 있다")
        fun create_withEmptyPermissions_shouldSucceed() {
            // GIVEN
            val request =
                PermissionCreateRequest(
                    "권한 없는 그룹",
                    "설명",
                    emptyList(), // 빈 리스트
                )

            // WHEN
            val groupId = permissionService.create(request)

            // THEN
            val foundGroup = permissionRepository.findById(groupId).orElseThrow()
            assertThat(foundGroup.name).isEqualTo("권한 없는 그룹")
            assertThat(foundGroup.resourcePermissions).isNotNull().isEmpty()
            assertThat(foundGroup.domainPermissions).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공: 모든 권한 조회 시 전체 목록이 반환된다")
        fun findAll_shouldReturnAllPermissions() {
            // GIVEN
            permissionService.create(createRequest)
            permissionService.create(PermissionCreateRequest("추가 그룹", null, emptyList()))

            // WHEN
            val responses = permissionService.findAll()

            // THEN
            assertThat(responses).hasSize(2)
        }

        @Test
        @DisplayName("성공: 권한 그룹이 없을 때 전체 조회 시 빈 리스트가 반환된다")
        fun findAll_whenNoGroupsExist_shouldReturnEmptyList() {
            // GIVEN: 데이터가 없는 상태

            // WHEN
            val responses = permissionService.findAll()

            // THEN
            assertThat(responses).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공: 권한을 빈 리스트로 업데이트하여 모든 권한을 제거할 수 있다")
        fun update_withEmptyPermissionList_shouldRemoveAllPermissions() {
            // GIVEN
            val groupId = permissionService.create(createRequest)
            val existingGroup = permissionRepository.findById(groupId).orElseThrow()
            assertThat(existingGroup.resourcePermissions).isNotEmpty()

            val updateRequest =
                PermissionUpdateRequest(
                    "권한 제거된 그룹",
                    null,
                    emptyList(), // 빈 리스트로 업데이트
                )

            // WHEN
            permissionService.update(groupId, updateRequest)

            // THEN
            val updatedGroup = permissionRepository.findById(groupId).orElseThrow()
            assertThat(updatedGroup.name).isEqualTo("권한 제거된 그룹")
            assertThat(updatedGroup.resourcePermissions).isEmpty()
            assertThat(updatedGroup.domainPermissions).isEmpty()
        }

        @Test
        @DisplayName("성공: 권한 목록 변경 없이 이름과 설명만 수정할 수 있다")
        fun update_onlyNameAndDescription_shouldSucceed() {
            // GIVEN
            val groupId = permissionService.create(createRequest)
            val permission = permissionService.findPermissionById(groupId)

            val permissionRequests =
                permission.resourcePermissions.map { permission ->
                    PermissionRequest(permission.resourceName, listOf(permission.resourceId), permission.level)
                }
            val updateRequest = PermissionUpdateRequest("이름만 변경", "설명만 변경", permissionRequests)

            // WHEN
            permissionService.update(groupId, updateRequest)

            // THEN
            val updatedGroup = permissionRepository.findById(groupId).orElseThrow()
            assertThat(updatedGroup.name).isEqualTo("이름만 변경")
            assertThat(updatedGroup.description).isEqualTo("설명만 변경")
            assertThat(updatedGroup.resourcePermissions).hasSize(3)
            assertThat(updatedGroup.domainPermissions).isEmpty()
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 업데이트 시도 시 예외가 발생한다")
        fun update_withNonExistingId_shouldThrowException() {
            // GIVEN
            val request = PermissionUpdateRequest("이름", "설명", emptyList())

            // WHEN & THEN
            assertThrows<CustomException> {
                permissionService.update(9999L, request)
            }
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 삭제 시도 시 예외가 발생한다")
        fun delete_withNonExistingId_shouldThrowException() {
            // WHEN & THEN
            assertThrows<CustomException> {
                permissionService.delete(9999L)
            }
        }
    }
