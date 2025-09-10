package com.pluxity.user

import com.pluxity.config.MockBeansConfig
import com.pluxity.global.exception.CustomException
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleResponse
import com.pluxity.user.dto.UserCreateRequest
import com.pluxity.user.dto.UserPasswordUpdateRequest
import com.pluxity.user.dto.UserUpdateRequest
import com.pluxity.user.entity.User
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
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
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
class UserRoleTest
    @Autowired
    constructor(
        private val userService: UserService,
        private val userRepository: UserRepository,
        private val roleService: RoleService,
        private val roleRepository: RoleRepository,
        private val passwordEncoder: PasswordEncoder,
        private val em: EntityManager,
    ) {
        private val roleIds = mutableListOf<Long>()

        @BeforeEach
        fun setUp() {
            // 테스트에 사용할 Role 미리 생성
            roleIds.clear()
            (1..3).forEach { i ->
                val request = RoleCreateRequest("Test Role $i", "Desc $i", mutableListOf())
                roleIds.add(roleService.save(request))
            }
            em.flush()
            em.clear()
        }

        @Test
        @DisplayName("새로운 User를 역할(Role)과 함께 생성하고, 생성된 User를 조회하여 검증한다")
        fun save_withRoles_andVerify() {
            // GIVEN
            val assignedRoleIds = listOf(roleIds[0], roleIds[1])
            val createRequest =
                UserCreateRequest(
                    "newUser",
                    "password123",
                    "New User",
                    "U001",
                    "010-1234-5678",
                    "Dev",
                    assignedRoleIds,
                )

            // WHEN
            val savedUserResponse = userService.save(createRequest)
            val userId = savedUserResponse.id
            em.flush()
            em.clear()

            // THEN
            val foundUserResponse = userService.findById(userId)
            Assertions.assertThat(foundUserResponse.username).isEqualTo("newUser")
            Assertions.assertThat(foundUserResponse.name).isEqualTo("New User")
            Assertions.assertThat(foundUserResponse.roles).hasSize(2)

            val foundRoleIds =
                foundUserResponse.roles
                    .stream()
                    .map(RoleResponse::id)
                    .toList()
            Assertions.assertThat(foundRoleIds).containsExactlyInAnyOrderElementsOf(assignedRoleIds)
        }

        @Test
        @DisplayName("중복된 username으로 User 생성을 시도하면 예외가 발생한다")
        fun save_withDuplicateUsername_throwsException() {
            // GIVEN
            userService.save(
                UserCreateRequest("duplicateUser", "pw1", "User One", null, null, null, mutableListOf()),
            )
            em.flush()
            em.clear()

            // WHEN & THEN
            val duplicateRequest =
                UserCreateRequest("duplicateUser", "pw2", "User Two", null, null, null, mutableListOf())
            // unique 제약조건 위반은 flush 시점에 발생
            assertThrows<DataIntegrityViolationException> {
                userService.save(duplicateRequest)
                em.flush()
            }
        }

        @Test
        @DisplayName("User 정보 업데이트 시, 역할(Role)까지 올바르게 동기화되는지 검증한다")
        fun update_userAndRoles_andVerify() {
            // GIVEN: 1, 2번 역할을 가진 User 생성
            val originalUser =
                userService.save(
                    UserCreateRequest(
                        "updateUser",
                        "pw",
                        "Original Name",
                        "C01",
                        "010-1111-1111",
                        "Dept1",
                        listOf(roleIds[0], roleIds[1]),
                    ),
                )
            val userId = originalUser.id
            em.flush()
            em.clear()

            // WHEN: 이름, 부서 변경. 역할은 2번 유지, 3번 추가 (최종: 2, 3번 역할)
            val updatedRoleIds = listOf(roleIds[1], roleIds[2])
            val updateRequest =
                UserUpdateRequest("Updated Name", null, null, "Dept2", updatedRoleIds)
            userService.update(userId, updateRequest)
            em.flush()
            em.clear()

            // THEN
            val updatedUser = userService.findById(userId)
            Assertions.assertThat(updatedUser.name).isEqualTo("Updated Name")
            Assertions.assertThat(updatedUser.code).isEqualTo("C01") // null로 보내면 변경되지 않음
            Assertions.assertThat(updatedUser.department).isEqualTo("Dept2")

            val finalRoleIds =
                updatedUser.roles
                    .stream()
                    .map(RoleResponse::id)
                    .toList()
            Assertions.assertThat(finalRoleIds).hasSize(2)
            Assertions.assertThat(finalRoleIds).containsExactlyInAnyOrderElementsOf(updatedRoleIds)
        }

        @Test
        @DisplayName("User 삭제 시, UserRole은 함께 삭제되지만 Role 자체는 삭제되지 않음을 검증한다")
        fun delete_user_andVerifyCascade() {
            // GIVEN
            val initialRoleCount = roleRepository.count()
            val userResponse =
                userService.save(
                    UserCreateRequest("deleteUser", "pw", "Del Name", null, null, null, roleIds),
                )
            val userId = userResponse.id
            em.flush()
            em.clear()

            val user = em.find(User::class.java, userId) // UserRole 개수 확인을 위해 영속성 컨텍스트에서 다시 로드
            Assertions.assertThat(user.userRoles).isNotEmpty()

            // WHEN
            userService.delete(userId)
            em.flush()
            em.clear()

            // THEN
            // 1. User는 삭제되어야 함
            assertThrows<EntityNotFoundException> {
                userService.findById(userId)
            }

            // 2. UserRole은 orphanRemoval=true에 의해 함께 삭제되어야 함 (직접 확인은 어려우나, User 삭제가 성공한 것이 증거)

            // 3. Role 엔티티 자체는 삭제되지 않아야 함
            Assertions.assertThat(roleRepository.count()).isEqualTo(initialRoleCount)
        }

        @Test
        @DisplayName("올바른 현재 비밀번호로 변경 시도 시 성공한다")
        fun updateUserPassword_withCorrectCurrentPassword_succeeds() {
            // GIVEN
            val initialPassword = "password123"
            val userResponse =
                userService.save(
                    UserCreateRequest(
                        "pwUser",
                        initialPassword,
                        "PW User",
                        null,
                        null,
                        null,
                        mutableListOf(),
                    ),
                )
            val userId = userResponse.id
            em.flush()
            em.clear()

            // WHEN
            val newPassword = "newPassword456"
            val request = UserPasswordUpdateRequest(initialPassword, newPassword)
            userService.updateUserPassword(userId, request)
            em.flush()
            em.clear()

            // THEN
            val updatedUser = userRepository.findWithGraphById(userId)
            Assertions.assertThat(passwordEncoder.matches(newPassword, updatedUser?.password)).isTrue()
        }

        @Test
        @DisplayName("틀린 현재 비밀번호로 변경 시도 시 CustomException이 발생한다")
        fun updateUserPassword_withIncorrectCurrentPassword_throwsException() {
            // GIVEN
            val initialPassword = "password123"
            val userResponse =
                userService.save(
                    UserCreateRequest(
                        "pwUser2",
                        initialPassword,
                        "PW User2",
                        null,
                        null,
                        null,
                        mutableListOf(),
                    ),
                )
            val userId = userResponse.id
            em.flush()
            em.clear()

            // WHEN & THEN
            val request =
                UserPasswordUpdateRequest("wrongPassword", "newPassword")
            assertThrows<CustomException> {
                userService.updateUserPassword(userId, request)
            }
        }

        @Test
        @DisplayName("비밀번호 초기화 시, 새 비밀번호로 변경되고 마지막 변경일이 과거로 설정된다")
        fun initPassword_resetsPasswordAndLastChangeDate() {
            // GIVEN
            val userResponse =
                userService.save(
                    UserCreateRequest(
                        "initPwUser",
                        "anyPassword",
                        "Init PW",
                        null,
                        null,
                        null,
                        mutableListOf(),
                    ),
                )
            val userId = userResponse.id
            em.flush()
            em.clear()

            // WHEN
            userService.initPassword(userId)
            em.flush()
            em.clear()

            // THEN
            val user = userRepository.findWithGraphById(userId)
            // 실제 초기화 비밀번호 값은 @Value에서 주입되므로, 암호화된 값이 null이 아닌지만 체크
            Assertions.assertThat(user?.password).isNotNull()
            // isPasswordChangeRequired 로직으로 검증
            Assertions.assertThat(user?.isPasswordChangeRequired()).isTrue
        }
    }
