package com.pluxity.user.entity

import com.pluxity.authentication.entity.RefreshToken
import org.springframework.test.util.ReflectionTestUtils

fun dummyUser(
    id: Long? = 1L,
    username: String = "username",
    password: String = "password",
    name: String = "name",
    code: String = "code",
    phoneNumber: String? = null,
    department: String? = null,
): User = User(id, username, password, name, code, phoneNumber, department)

fun dummyRole(
    id: Long? = 1L,
    name: String = "name",
    description: String = "description",
): Role {
    val retRole = Role(id, name, description)
    ReflectionTestUtils.setField(retRole, "id", id)
    return retRole
}

fun dummyRefreshToken(
    username: String = "username",
    token: String = "token",
    timeToLive: Int = 30,
): RefreshToken = RefreshToken(username, token, timeToLive)
