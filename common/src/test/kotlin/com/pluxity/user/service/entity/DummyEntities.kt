package com.pluxity.user.service.entity

import com.pluxity.user.entity.Role
import com.pluxity.user.entity.User
import com.pluxity.user.entity.UserRole
import com.pluxity.user.entity.dummyUser

fun dummyRole(
    id: Long? = 1L,
    name: String = "role",
    description: String? = "description",
) = Role(id, name, description)

fun dummyUserRoles(
    id: Long? = 1L,
    user: User = dummyUser(),
    role: Role = dummyRole(),
) = UserRole(id, user, role)
