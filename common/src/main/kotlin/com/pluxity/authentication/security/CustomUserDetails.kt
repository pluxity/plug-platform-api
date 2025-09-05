package com.pluxity.authentication.security

import com.pluxity.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomUserDetails(
    val user: User,
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        user.getRoles().map { role -> SimpleGrantedAuthority(role.getAuthority()) }.toMutableList()

    override fun getPassword(): String = user.password

    override fun getUsername(): String = user.username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
