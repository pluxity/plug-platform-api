package com.pluxity.user.service

import com.pluxity.authentication.repository.RefreshTokenRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.user.dto.dummyUserCreateRequest
import com.pluxity.user.dto.dummyUserPasswordUpdateRequest
import com.pluxity.user.dto.dummyUserRoleAssignRequest
import com.pluxity.user.dto.dummyUserUpdateRequest
import com.pluxity.user.entity.dummyRefreshToken
import com.pluxity.user.entity.dummyRole
import com.pluxity.user.entity.dummyUser
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
import com.pluxity.user.repository.UserRoleRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceKoTest :
    BehaviorSpec({
        val userRepository: UserRepository = mockk()
        val roleRepository: RoleRepository = mockk()
        val passwordEncoder: PasswordEncoder = mockk()
        val refreshTokenRepository: RefreshTokenRepository = mockk()
        val userRoleRepository: UserRoleRepository = mockk()
        val userService =
            UserService(
                userRepository,
                roleRepository,
                passwordEncoder,
                refreshTokenRepository,
                userRoleRepository,
            )

        Given("사용자 상세 조회를 진행할 때") {

            When("유효한 아이디로 조회 요청") {
                val user = dummyUser()
                every { userRepository.findWithGraphById(any()) } returns user
                Then("정상 조회") {
                    val res = userService.findById(user.id!!)
                    res.id shouldBe user.id
                    res.name shouldBe user.name
                }
            }

            When("없는 아이디로 조회 요청") {
                val id = 999L
                every { userRepository.findWithGraphById(any()) } returns null
                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<EntityNotFoundException> {
                        userService.findById(id)
                    }.message shouldBe "User not found with id: $id"
                }
            }
        }

        Given("사용자 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                every {
                    userRepository.findAllBy(any())
                } returns mutableListOf(dummyUser())

                Then("성공") {
                    userService.findAll().size shouldBe 1
                }
            }
        }

        Given("사용자 username으로 조회를 진행할 때") {

            When("유효한 username으로 조회 요청") {
                val user = dummyUser()
                every { userRepository.findByUsername(any()) } returns user
                Then("정상 조회") {
                    val res = userService.findByUsername(user.username)
                    res.id shouldBe user.id
                    res.username shouldBe user.username
                }
            }

            When("없는 username으로 조회 요청") {
                every { userRepository.findByUsername(any()) } returns null
                Then("NOT_FOUND_DEVICE 예외 발생") {
                    val userName = "targetUser"
                    shouldThrowExactly<EntityNotFoundException> {
                        userService.findByUsername(userName)
                    }.message shouldBe "User not found with username: $userName"
                }
            }
        }

        Given("사용자 생성을 진행할 때") {
            When("role이 없는 요청으로 사용자 생성 요청") {
                val createRequest = dummyUserCreateRequest()
                val user =
                    dummyUser(
                        name = createRequest.name,
                        code = createRequest.code!!,
                        password = createRequest.password,
                        username = createRequest.username,
                    )

                every { userRepository.save(any()) } returns user

                every { passwordEncoder.encode(any()) } returns ""

                Then("성공") {
                    val res = userService.save(createRequest)
                    res.id shouldBe user.id
                }
            }

            When("role이 있는 요청으로 사용자 생성 요청") {
                val createRequest = dummyUserCreateRequest(roleIds = listOf(1))
                val user =
                    dummyUser(
                        name = createRequest.name,
                        code = createRequest.code!!,
                        password = createRequest.password,
                        username = createRequest.username,
                    )
                val role = dummyRole()
                user.addRole(role)

                every {
                    userRepository.save(any())
                } returns user

                every {
                    passwordEncoder.encode(any())
                } returns ""

                every {
                    roleRepository.findByIdOrNull(any())
                } returns role

                Then("성공") {
                    val res = userService.save(createRequest)
                    res.id shouldBe user.id
                    res.roles.size shouldBe 1
                    res.roles.first().name shouldBe role.name
                }
            }
        }

        Given("사용자 수정을 진행할 때") {
            When("role이 없는 정상 수정 요청") {
                val updateRequest = dummyUserUpdateRequest(name = "updateName")
                val user = dummyUser()
                every {
                    userRepository.findWithGraphById(any())
                } returns user

                Then("성공") {
                    val res = userService.update(user.id!!, updateRequest)
                    res.name shouldBe updateRequest.name
                }
            }
            When("role이 있는 정상 수정 요청") {
                val updateRequest = dummyUserUpdateRequest(name = "updateName", roleIds = listOf(1))
                val user = dummyUser()
                val role = dummyRole()
                user.addRole(role)

                every { userRepository.findWithGraphById(any()) } returns user
                every { roleRepository.findAllById(any()) } returns listOf(role)
                every { userRoleRepository.deleteAll(any()) } just runs

                Then("성공") {
                    val res = userService.update(user.id!!, updateRequest)
                    res.name shouldBe updateRequest.name
                }
            }
        }

        Given("사용자 삭제를 진행할 때") {

            When("유효한 아이디로 조회 요청") {
                val user = dummyUser()
                every { userRepository.findWithGraphById(any()) } returns user
                every { userRoleRepository.deleteAllByUser(any()) } just runs
                every { userRepository.delete(any()) } just runs

                Then("성공") {
                    userService.delete(user.id!!)
                    verify(exactly = 1) { userRoleRepository.deleteAllByUser(any()) }
                    verify(exactly = 1) { userRepository.delete(any()) }
                }
            }

            When("없는 아이디로 조회 요청") {
                val id = 999L
                every { userRepository.findWithGraphById(any()) } returns null
                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<EntityNotFoundException> {
                        userService.findById(id)
                    }.message shouldBe "User not found with id: $id"
                }
            }
        }

        Given("사용자에 역할 할당을 진행할 때") {
            When("유효한 요청으로 할당 요청") {
                val user = dummyUser()
                var request = dummyUserRoleAssignRequest(roleIds = listOf(1))
                val role = dummyRole()

                every { userRepository.findWithGraphById(any()) } returns user
                every { roleRepository.findAllById(any()) } returns listOf(role)
                every { userRoleRepository.deleteAll(any()) } just runs

                Then("성공") {
                    userService.updateUserRoles(user.id!!, request)
                    user.getRoles().size shouldBe 1
                    user.getRoles().first().name shouldBe role.name
                }
            }
        }

        Given("사용자에 역할 제거를 진행할 때") {
            When("유효한 요청으로 제거 요청") {
                val user = dummyUser()
                val role = dummyRole()
                user.addRole(role)

                every { userRepository.findWithGraphById(any()) } returns user
                every { roleRepository.findByIdOrNull(any()) } returns role

                Then("성공") {
                    userService.removeRoleFromUser(user.id!!, role.id!!)
                    user.getRoles().size shouldBe 0
                }
            }
        }

        Given("관리자가 사용자의 비밀번호 변경을 진행할 때") {
            When("유효한 요청으로 변경 요청") {
                val user = dummyUser()
                val request = dummyUserPasswordUpdateRequest()

                every { userRepository.findWithGraphById(any()) } returns user
                every { passwordEncoder.matches(any(), any()) } returns true
                every { passwordEncoder.encode(any()) } returns request.newPassword

                Then("성공") {
                    userService.updateUserPassword(user.id!!, request)
                    user.password shouldBe request.newPassword
                }
            }

            When("현재 비밀번호가 일치 하지 않는 요청으로 변경 요청") {
                val user = dummyUser()
                val request = dummyUserPasswordUpdateRequest()

                every { userRepository.findWithGraphById(any()) } returns user
                every { passwordEncoder.matches(any(), any()) } returns false

                Then("CustomException 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        userService.updateUserPassword(user.id!!, request)
                    }.message shouldBe ErrorCode.INVALID_ID_OR_PASSWORD.getMessage().format(user.id)
                }
            }
        }

        Given("로그인된 사용자 목록 조회를 진행할 때") {
            When("유효한 요청으로 조회 요청") {
                val user = dummyUser()
                val token = dummyRefreshToken()

                every { userRepository.findAllBy(any()) } returns listOf(user)
                every { refreshTokenRepository.findByIdOrNull(any()) } returns token

                Then("성공") {
                    val res = userService.isLoggedIn()
                    res.size shouldBe 1
                    res.first().isLoggedIn shouldBe true
                }
            }
        }

        Given("사용자 비밀번호 초기화를 진행할 때") {
            When("유효한 요청으로 초기화 요청") {
                val user = dummyUser()
                val initPassword = "initPassword"

                every { userRepository.findWithGraphById(any()) } returns user
                every { passwordEncoder.encode(any()) } returns initPassword

                Then("성공") {
                    userService.initPassword(user.id!!)
                    user.password shouldBe initPassword
                }
            }

            When("없는 아이디로 조회 요청") {
                val id = 999L
                every { userRepository.findWithGraphById(any()) } returns null
                Then("NOT_FOUND_DEVICE 예외 발생") {
                    shouldThrowExactly<EntityNotFoundException> {
                        userService.initPassword(id)
                    }.message shouldBe "User not found with id: $id"
                }
            }
        }

        Given("사용자가 자신의 비밀번호 변경을 진행할 때") {
            When("유효한 요청으로 변경 요청") {
                val user = dummyUser()
                val request = dummyUserPasswordUpdateRequest()

                every { userRepository.findByUsername(any()) } returns user
                every { userRepository.findWithGraphById(any()) } returns user
                every { passwordEncoder.matches(any(), any()) } returns true
                every { passwordEncoder.encode(any()) } returns request.newPassword

                Then("성공") {
                    userService.updateUserPassword(user.id!!, request)
                    user.password shouldBe request.newPassword
                }
            }

            When("현재 비밀번호가 일치 하지 않는 요청으로 변경 요청") {
                val user = dummyUser()
                val request = dummyUserPasswordUpdateRequest()

                every { userRepository.findByUsername(any()) } returns null
                every { userRepository.findWithGraphById(any()) } returns user
                every { passwordEncoder.matches(any(), any()) } returns false

                Then("CustomException 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        userService.updateUserPassword(user.id!!, request)
                    }.message shouldBe ErrorCode.INVALID_ID_OR_PASSWORD.getMessage().format(user.id)
                }
            }
        }
    })
