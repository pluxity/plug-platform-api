package com.pluxity.authentication.security

import com.pluxity.user.entity.Role
import com.pluxity.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@JvmRecord
data class CustomUserDetails(val user: User?) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority?> {
        return user!!.getRoles().stream()
            .map<SimpleGrantedAuthority?> { role: Role? -> SimpleGrantedAuthority(role!!.getAuthority()) }
            .toList()
    }

    override fun getPassword(): String {
        return user!!.password
    }

    override fun getUsername(): String {
        return user!!.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
