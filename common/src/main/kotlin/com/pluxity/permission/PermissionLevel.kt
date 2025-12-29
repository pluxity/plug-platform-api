package com.pluxity.permission

enum class PermissionLevel(
    private val rank: Int,
) {
    READ(1),
    WRITE(2),
    ADMIN(3),
    ;

    fun allows(required: PermissionLevel): Boolean = this.rank >= required.rank
}
