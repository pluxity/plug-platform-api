package com.pluxity.user.service;


import com.pluxity.permission.*;
import com.pluxity.permission.dto.PermissionRequest;
import com.pluxity.permission.ResourceType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.permission.dto.PermissionGroupCreateRequest;
import com.pluxity.permission.dto.PermissionGroupResponse;
import com.pluxity.permission.dto.PermissionGroupUpdateRequest;
import com.pluxity.user.repository.RolePermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PermissionGroupServiceTest {

    @Autowired private PermissionGroupService permissionGroupService;
    @Autowired private PermissionGroupRepository permissionGroupRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private RolePermissionRepository rolePermissionRepository; // delete 테스트를 위해 주입

    private PermissionGroupCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // 여러 테스트에서 사용할 기본 생성 요청 DTO
        createRequest =
                new PermissionGroupCreateRequest(
                        "기본 시설 관리 그룹",
                        "시설에 대한 기본 권한",
                        List.of(
                                new PermissionRequest(ResourceType.FACILITY.name(), List.of("READ", "LIST")),
                                new PermissionRequest(ResourceType.DEVICE_CATEGORY.name(), List.of("READ"))));
    }

    @Nested
    @DisplayName("권한 그룹 생성 (Create)")
    class CreatePermissionGroup {

        @Test
        @DisplayName("성공: 유효한 요청으로 권한 그룹 생성 시, 그룹과 모든 하위 권한들이 올바르게 저장된다")
        void withValidRequest_shouldSaveGroupAndAllPermissions() {
            // when
            Long groupId = permissionGroupService.create(createRequest);

            // then
            assertThat(groupId).isNotNull();

            // 저장된 그룹 확인
            PermissionGroup foundGroup = permissionGroupRepository.findById(groupId).orElseThrow();
            assertThat(foundGroup.getName()).isEqualTo("기본 시설 관리 그룹");
            assertThat(foundGroup.getDescription()).isEqualTo("시설에 대한 기본 권한");

            // 저장된 권한 확인
            Set<Permission> permissions = foundGroup.getPermissions();
            assertThat(permissions).hasSize(3);

            // FACILITY 권한 검증
            assertThat(permissions.stream()
                    .filter(p -> p.getResourceName().equals("FACILITY"))
                    .map(Permission::getResourceId))
                    .containsExactlyInAnyOrder("READ", "LIST");

            // DEVICE_CATEGORY 권한 검증
            assertThat(permissions.stream()
                    .filter(p -> p.getResourceName().equals("DEVICE_CATEGORY"))
                    .map(Permission::getResourceId))
                    .containsExactly("READ");
        }

        @Test
        @DisplayName("실패: 중복된 그룹 이름으로 생성 시도 시 DUPLICATE_PERMISSION_GROUP_NAME 예외가 발생한다")
        void withDuplicateGroupName_shouldThrowException() {
            // given
            permissionGroupService.create(createRequest); // 먼저 하나 생성
            PermissionGroupCreateRequest duplicateRequest = new PermissionGroupCreateRequest(
                    "기본 시설 관리 그룹", // 중복된 이름
                    "다른 설명",
                    List.of(new PermissionRequest("PARK", List.of("VIEW")))
            );

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> permissionGroupService.create(duplicateRequest));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_PERMISSION_GROUP_NAME);
        }

        @Test
        @DisplayName("실패: 요청 DTO의 한 권한 목록 내에 중복된 리소스 ID가 포함된 경우 DUPLICATE_RESOURCE_ID 예외가 발생한다")
        void withDuplicateResourceIdsInRequest_shouldThrowException() {
            // given
            PermissionGroupCreateRequest duplicateRequest = new PermissionGroupCreateRequest(
                    "잘못된 그룹",
                    "설명",
                    List.of(new PermissionRequest("FACILITY", List.of("READ", "LIST", "READ"))) // 중복
            );

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> permissionGroupService.create(duplicateRequest));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE_ID);
        }
    }

    @Nested
    @DisplayName("권한 그룹 조회 (Read)")
    class ReadPermissionGroup {

        private Long groupId;

        @BeforeEach
        void setUp() {
            groupId = permissionGroupService.create(createRequest);
        }

        @Test
        @DisplayName("성공: 존재하는 ID로 조회 시 그룹 정보와 하위 권한들이 DTO로 반환된다")
        void findById_withExistingId_shouldReturnResponse() {
            // when
            PermissionGroupResponse response = permissionGroupService.findById(groupId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(groupId);
            assertThat(response.name()).isEqualTo("기본 시설 관리 그룹");
            assertThat(response.permissions()).hasSize(3);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 조회 시 NOT_FOUND_PERMISSION_GROUP 예외가 발생한다")
        void findById_withNonExistingId_shouldThrowException() {
            // given
            Long nonExistingId = 9999L;

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> permissionGroupService.findById(nonExistingId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_PERMISSION_GROUP);
        }
    }


    @Nested
    @DisplayName("권한 그룹 수정 (Update - PATCH 방식)")
    class UpdatePermissionGroup {
        private Long groupId;

        @BeforeEach
        void setUp() {
            // 수정된 ResourceType과 일치하는 테스트 데이터 생성
            PermissionGroupCreateRequest createRequest = new PermissionGroupCreateRequest(
                    "기본 시설 관리 그룹",
                    "시설 및 장비 분류에 대한 기본 권한",
                    List.of(
                            // name()을 사용하여 "시설" 문자열을 전달
                            new PermissionRequest(ResourceType.FACILITY.name(), List.of("READ", "LIST")),
                            // name()을 사용하여 "장비 분류" 문자열을 전달
                            new PermissionRequest(ResourceType.DEVICE_CATEGORY.name(), List.of("READ"))
                    )
            );
            groupId = permissionGroupService.create(createRequest);
        }

        @Test
        @DisplayName("성공: 권한을 추가, 유지, 삭제하는 복합적인 수정이 정상적으로 반영된다")
        void withValidRequest_shouldPatchPermissionsCorrectly() {
            // given
            // 기존 상태: FACILITY(READ, LIST), DEVICE_CATEGORY(READ)
            // 목표 상태: FACILITY(EDIT, LIST), DEVICE_CATEGORY(CREATE)
            // 변경 내역: FACILITY(READ) 삭제, DEVICE_CATEGORY(READ) 삭제, FACILITY(EDIT) 추가, DEVICE_CATEGORY(CREATE) 추가
            PermissionGroupUpdateRequest updateRequest = new PermissionGroupUpdateRequest(
                    "수정된 고급 그룹",
                    "수정된 설명입니다.",
                    List.of(
                            // "시설" 문자열로 요청
                            new PermissionRequest(ResourceType.FACILITY.name(), List.of("EDIT", "LIST")),
                            // "장비 분류" 문자열로 요청
                            new PermissionRequest(ResourceType.DEVICE_CATEGORY.name(), List.of("CREATE"))
                    )
            );

            // when
            permissionGroupService.update(groupId, updateRequest);

            // then
            PermissionGroup updatedGroup = permissionGroupRepository.findById(groupId).orElseThrow();
            assertThat(updatedGroup.getName()).isEqualTo("수정된 고급 그룹");
            assertThat(updatedGroup.getDescription()).isEqualTo("수정된 설명입니다.");

            Set<Permission> permissions = updatedGroup.getPermissions();
            assertThat(permissions).hasSize(3); // 최종 3개 (FACILITY:EDIT, FACILITY:LIST, DEVICE_CATEGORY:CREATE)

            Set<String> permissionKeys = permissions.stream()
                    .map(p -> p.getResourceName() + ":" + p.getResourceId())
                    .collect(Collectors.toSet());

            // 최종 상태 검증
            assertThat(permissionKeys).containsExactlyInAnyOrder(
                    "FACILITY:EDIT",         // 추가됨
                    "FACILITY:LIST",         // 유지됨
                    "DEVICE_CATEGORY:CREATE" // 추가됨
            );

            // 삭제된 권한 검증
            assertThat(permissionKeys).doesNotContain(
                    "FACILITY:READ",        // 삭제됨
                    "DEVICE_CATEGORY:READ"  // 삭제됨
            );
        }

        @Test
        @DisplayName("실패: 다른 그룹과 중복되는 이름으로 수정 시도 시 예외가 발생한다")
        void withDuplicateGroupName_shouldThrowException() {
            // given
            // 비교 대상 그룹 생성
            PermissionGroupCreateRequest anotherRequest = new PermissionGroupCreateRequest(
                    "다른 그룹", "다른 설명",
                    List.of(new PermissionRequest(ResourceType.FACILITY.name(), List.of("P1")))
            );
            permissionGroupService.create(anotherRequest);

            // 기존 그룹을 '다른 그룹'과 동일한 이름으로 업데이트 시도
            PermissionGroupUpdateRequest updateRequest = new PermissionGroupUpdateRequest(
                    "다른 그룹", // 중복되는 이름
                    "설명",
                    List.of()
            );

            // when & then
            assertThrows(CustomException.class, () -> permissionGroupService.update(groupId, updateRequest));
        }
    }

    @Nested
    @DisplayName("권한 그룹 삭제 (Delete)")
    class DeletePermissionGroup {
        private Long groupId;

        @BeforeEach
        void setUp() {
            groupId = permissionGroupService.create(createRequest);
        }

        @Test
        @DisplayName("성공: 존재하는 ID의 그룹 삭제 시, 그룹과 하위 권한들이 모두 삭제된다")
        void withExistingId_shouldDeleteGroupAndPermissions() {
            // given
            long initialPermissionCount = permissionRepository.count();
            assertThat(permissionGroupRepository.existsById(groupId)).isTrue();

            // when
            permissionGroupService.delete(groupId);

            // then
            assertThat(permissionGroupRepository.existsById(groupId)).isFalse();
            // 그룹에 속해있던 3개의 권한이 삭제되었는지 확인
            assertThat(permissionRepository.count()).isEqualTo(initialPermissionCount - 3);
        }
    }
}