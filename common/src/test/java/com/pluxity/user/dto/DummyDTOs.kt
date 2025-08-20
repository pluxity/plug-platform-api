package com.pluxity.user.dto

fun dummyUserCreateRequest(
    username: String = "username",
    password: String = "password",
    name: String = "name",
    code: String = "code",
    phoneNumber: String? = null,
    department: String? = null,
    roleIds: List<Long>? = listOf(),
): UserCreateRequest =
    UserCreateRequest(
        username,
        password,
        name,
        code,
        phoneNumber,
        department,
        roleIds,
    )

fun dummyUserUpdateRequest(
    name: String = "name",
    code: String = "code",
    phoneNumber: String? = null,
    department: String? = null,
    roleIds: List<Long>? = null,
): UserUpdateRequest = UserUpdateRequest(name, code, phoneNumber, department, roleIds)

fun dummyUserRoleAssignRequest(roleIds: List<Long> = listOf()): UserRoleUpdateRequest = UserRoleUpdateRequest(roleIds)

fun dummyUserPasswordUpdateRequest(
    currentPw: String = "currentPw",
    newPw: String = "newPw",
): UserPasswordUpdateRequest = UserPasswordUpdateRequest(currentPw, newPw)
