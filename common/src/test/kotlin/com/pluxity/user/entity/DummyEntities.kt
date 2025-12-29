package com.pluxity.user.entity

import base.entity.withId
import com.pluxity.authentication.entity.RefreshToken
import com.pluxity.permission.ResourceType

fun dummyUser(
    id: Long? = 1L,
    username: String = "username",
    password: String = "password",
    name: String = "name",
    code: String? = "code",
    phoneNumber: String? = null,
    department: String? = null,
): User = User(username, password, name, code, phoneNumber, department).withId(id)

fun dummyRole(
    id: Long? = 1L,
    name: String = "name",
    description: String? = "description",
): Role = Role(name, description).withId(id)

fun dummyRefreshToken(
    username: String = "username",
    token: String = "token",
    timeToLive: Int = 30,
): RefreshToken = RefreshToken(username, token, timeToLive)

fun dummyRoleGlobalPolicy(
    id: Long? = 1L,
    role: Role,
    resourceType: ResourceType = ResourceType.FACILITY,
) = RoleGlobalPolicy(
    role = role,
    resourceType = resourceType,
).withId(id)
