package com.pluxity.user

import com.pluxity.authentication.entity.RefreshToken
import com.pluxity.authentication.repository.RefreshTokenRepository
import com.pluxity.global.exception.CustomException
import com.pluxity.user.dto.UserCreateRequest
import com.pluxity.user.dto.UserPasswordUpdateRequest
import com.pluxity.user.dto.UserRoleUpdateRequest
import com.pluxity.user.dto.UserUpdateRequest
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.User
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
import com.pluxity.user.service.UserService
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class UserServiceTest
    @Autowired
    constructor(
        private val userService: UserService,
        private val userRepository: UserRepository,
        private val roleRepository: RoleRepository,
        private val passwordEncoder: PasswordEncoder,
        private val refreshTokenRepository: RefreshTokenRepository,
    ) {
        // 테스트에서 공통으로 사용할 Role 엔티티
        private lateinit var roleUser: Role
        private lateinit var roleAdmin: Role

        @BeforeEach
        fun setUp() {
            // 모든 테스트 실행 전, 모든 데이터를 초기화하여 독립성 보장
            userRepository.deleteAll()
            roleRepository.deleteAll()
            refreshTokenRepository.deleteAll()

            // 공통으로 사용할 역할(Role) 생성
            roleUser = roleRepository.save(Role("ROLE_USER", "description of role_user"))
            roleAdmin = roleRepository.save(Role("ROLE_ADMIN", "description of role_admin"))
        }

        /** 테스트용 사용자를 생성하고 DB에 저장하는 헬퍼 메서드 */
        private fun createUser(
            username: String,
            name: String,
            code: String,
            roles: List<Role>,
        ): User {
            val user =
                User(
                    id = null,
                    username = username,
                    password = passwordEncoder.encode("password123"),
                    name = name,
                    code = code,
                    phoneNumber = "010-0000-0000",
                    department = "테스트부서",
                ).apply {
                    updateRoles(roles)
                }
            return userRepository.save(user)
        }

        @Test
        @DisplayName("성공: ID로 사용자 조회 시 정확한 사용자 정보를 반환한다")
        fun findById_Success() {
            // given
            val savedUser = createUser("testuser", "테스트유저", "T001", listOf(roleUser))

            // when
            val response = userService.findById(savedUser.id!!)

            // then
            assertThat(response.id).isEqualTo(savedUser.id)
            assertThat(response.username).isEqualTo("testuser")
            assertThat(response.name).isEqualTo("테스트유저")
            assertThat(response.code).isEqualTo("T001")
            assertThat(response.phoneNumber).isEqualTo("010-0000-0000")
            assertThat(response.department).isEqualTo("테스트부서")
            assertThat(response.shouldChangePassword).isFalse()
            assertThat(response.roles).hasSize(1)
            assertThat(response.roles.first().name()).isEqualTo("ROLE_USER")
            assertThat(response.roles.first().description()).isEqualTo("description of role_user")
            assertThat(response.roles.first().permissions()).isEmpty()
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 사용자 조회 시 예외가 발생한다")
        fun findById_UserNotFound() {
            // when & then
            assertThatThrownBy {
                userService.findById(9999L)
            }.isInstanceOf(EntityNotFoundException::class.java)
        }

        @Test
        @DisplayName("성공: 사용자 생성 시 비밀번호는 암호화되고 DB에 저장된다")
        fun save_Success() {
            // given
            val request =
                UserCreateRequest(
                    username = "newuser",
                    password = "password123",
                    name = "신규유저",
                    code = "NEW001",
                )

            // when
            val response = userService.save(request)

            // then
            assertThat(response.id).isNotNull
            val foundUser = userRepository.findWithGraphById(response.id)!!

            assertThat(foundUser.username).isEqualTo("newuser")
            assertThat(passwordEncoder.matches("password123", foundUser.password)).isTrue()
        }

        @Test
        @DisplayName("성공: 사용자 정보 업데이트 시 변경된 내용이 DB에 반영된다")
        fun update_Success() {
            // given
            val savedUser = createUser("testuser", "원본이름", "ORI001", listOf())
            val request = UserUpdateRequest(name = "수정이름", department = "수정부서")

            // when
            userService.update(savedUser.id!!, request)

            // then
            val updatedUser = userRepository.findWithGraphById(savedUser.id!!)!!
            assertThat(updatedUser.name).isEqualTo("수정이름")
            assertThat(updatedUser.department).isEqualTo("수정부서")
            assertThat(updatedUser.code).isEqualTo("ORI001") // 변경되지 않은 필드는 유지
        }

        @Test
        @DisplayName("성공: 사용자 삭제 시 DB에서 해당 사용자가 삭제된다")
        fun delete_Success() {
            // given
            val savedUser = createUser("deleteuser", "삭제유저", "DEL001", listOf())
            val userId = savedUser.id!!
            assertThat(userRepository.existsById(userId)).isTrue()

            // when
            userService.delete(userId)

            // then
            assertThat(userRepository.existsById(userId)).isFalse()
        }

        @Test
        @DisplayName("성공: 사용자 비밀번호 업데이트 시 DB의 비밀번호가 변경된다")
        fun updateUserPassword_Success() {
            // given
            val savedUser = createUser("testuser", "테스트유저", "T001", listOf())
            val request = UserPasswordUpdateRequest("password123", "newPassword")

            // when
            userService.updateUserPassword(savedUser.id!!, request)

            // then
            val updatedUser = userRepository.findWithGraphById(savedUser.id!!)!!
            assertThat(passwordEncoder.matches("newPassword", updatedUser.password)).isTrue()
        }

        @Test
        @DisplayName("실패: 현재 비밀번호가 틀리면 비밀번호 업데이트 시 예외가 발생한다")
        fun updateUserPassword_withWrongCurrentPassword_throwsException() {
            // given
            val savedUser = createUser("testuser", "테스트유저", "T001", listOf())
            val request = UserPasswordUpdateRequest("wrongPassword", "newPassword")

            // when & then
            assertThatThrownBy {
                userService.updateUserPassword(savedUser.id!!, request)
            }.isInstanceOf(CustomException::class.java)
        }

        @Test
        @DisplayName("성공: 사용자에게 여러 역할을 할당하면 정상적으로 반영된다")
        fun assignRolesToUser_Success() {
            // given
            val savedUser = createUser("testuser", "테스트유저", "T001", listOf())
            val request = UserRoleUpdateRequest(listOf(roleUser.id!!, roleAdmin.id!!))

            // when
            userService.updateUserRoles(savedUser.id!!, request)

            // then
            val updatedUser = userRepository.findWithGraphById(savedUser.id!!)!!
            assertThat(updatedUser.getRoles()).hasSize(2)
            assertThat(updatedUser.getRoles().map { it.name })
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
        }

        @Test
        @DisplayName("성공: 사용자 역할 업데이트 시 기존 역할은 지워지고 새 역할만 남는다")
        fun updateUserRoles_Success() {
            // given
            val savedUser = createUser("testuser", "테스트유저", "T001", listOf(roleUser))
            val request = UserRoleUpdateRequest(listOf(roleAdmin.id!!))

            // when
            userService.updateUserRoles(savedUser.id!!, request)

            // then
            val updatedUser = userRepository.findWithGraphById(savedUser.id!!)!!
            assertThat(updatedUser.getRoles()).hasSize(1)
            assertThat(updatedUser.getRoles().first().name).isEqualTo("ROLE_ADMIN")
        }

        @Test
        @DisplayName("성공: isLoggedIn 호출 시 사용자의 로그인 상태를 정확히 반환한다")
        fun isLoggedIn_ReturnsCorrectLoginStatus() {
            // given
            val loggedInUser = createUser("loggedIn", "로그인유저", "L001", listOf(roleUser))
            val loggedOutUser = createUser("loggedOut", "로그아웃유저", "O001", listOf(roleAdmin))

            // 로그인한 사용자의 리프레시 토큰 저장
            refreshTokenRepository.save(
                RefreshToken.of(loggedInUser.username, "some-token-value", 60),
            )

            // when
            val responses = userService.isLoggedIn()

            // then
            assertThat(responses).hasSize(2)

            val loggedInResponse =
                responses.first {
                    it.username == "loggedIn"
                }
            assertThat(loggedInResponse.isLoggedIn).isTrue()
            assertThat(loggedInResponse.roles?.first()?.name).isEqualTo("ROLE_USER")

            val loggedOutResponse =
                responses.first {
                    it.username == "loggedOut"
                }
            assertThat(loggedOutResponse.isLoggedIn).isFalse()
            assertThat(loggedOutResponse.roles?.first()?.name).isEqualTo("ROLE_ADMIN")
        }
    }
