package com.pluxity.user.entity

enum class RoleType(
    val roleName: String,
) {
    ADMIN("ADMIN"),
    USER("USER"),
    ;

    companion object {
        fun fromRoleName(roleName: String): RoleType? = values().find { it.roleName == roleName }
    }
}
