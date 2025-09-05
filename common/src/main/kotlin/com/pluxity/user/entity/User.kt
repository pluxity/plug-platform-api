package com.pluxity.user.entity

import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var username: String,
    @Column(nullable = false)
    var password: String,
    @Column(nullable = false, length = 20)
    var name: String,
    @Column(length = 20)
    var code: String?,
    var phoneNumber: String? = null,
    var department: String? = null,
) : BaseEntity() {
    var lastPasswordChangeDate: LocalDateTime = LocalDateTime.now()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var userRoles: MutableSet<UserRole> = LinkedHashSet()

    fun changePassword(password: String) {
        this.password = password
        this.lastPasswordChangeDate = LocalDateTime.now()
    }

    fun addRoles(roles: List<Role>) {
        val duplicateRoles = roles.filter { this.hasRole(it) }

        if (!duplicateRoles.isEmpty()) {
            val duplicateNames = duplicateRoles.joinToString(", ") { it.name }

            throw IllegalStateException("Some roles already exist for this user: $duplicateNames")
        }

        for (role in roles) {
            addRole(role)
        }
    }

    fun addRole(role: Role) {
        check(!hasRole(role)) { "Role already exists for this user: ${role.name}" }
        val userRole = UserRole(user = this, role = role)
        this.userRoles.add(userRole)
    }

    fun removeRole(role: Role) {
        val userRoleToRemove =
            userRoles
                .firstOrNull { it.role == role }
                ?: throw IllegalStateException("Role not found for this user: ${role.name}")

        this.userRoles.remove(userRoleToRemove)
    }

    fun updateRoles(newRoles: List<Role>) {
        val newRoleIds = newRoles.map { it.id }.toSet()

        this.userRoles.removeIf { it.role.id !in newRoleIds }

        val currentRoleIds = this.userRoles.map { it.role.id }.toSet()

        newRoles
            .filter { !currentRoleIds.contains(it.id) }
            .forEach { addRole(it) }
    }

    fun getRoles(): List<Role> = userRoles.map { it.role }

    fun hasRole(role: Role): Boolean = userRoles.any { it.role.id == role.id }

    fun changeName(name: String) {
        this.name = name
    }

    fun changeCode(code: String) {
        this.code = code
    }

    fun changePhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
    }

    fun changeDepartment(department: String) {
        this.department = department
    }

    fun canAccess(
        resourceName: String,
        resourceId: String,
    ): Boolean {
        if (userRoles.any { "ADMIN".equals(it.role.name, ignoreCase = true) }
        ) {
            return true
        }

        return userRoles.any { it.role.hasPermissionFor(resourceName, resourceId) }
    }

    fun isPasswordChangeRequired(): Boolean =
        lastPasswordChangeDate.isBefore(
            LocalDateTime
                .now()
                .minusDays(PASSWORD_CHANGE_DAYS),
        )

    fun initPassword(password: String) {
        this.password = password
        this.lastPasswordChangeDate = LocalDateTime.now().minusDays(INIT_PASSWORD_CHANGE_DAY)
    }

    companion object {
        private const val PASSWORD_CHANGE_DAYS: Long = 90L // 비밀번호 변경 주기
        private const val INIT_PASSWORD_CHANGE_DAY: Long =
            PASSWORD_CHANGE_DAYS + 1 // 비밀번호 초기화 시 비밀번호 변경 알림을 위해 변경일을 1일 추가한 날짜 전으로 설정
    }
}
