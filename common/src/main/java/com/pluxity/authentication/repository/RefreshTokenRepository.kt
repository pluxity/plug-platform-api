package com.pluxity.authentication.repository

import com.pluxity.authentication.entity.RefreshToken
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface RefreshTokenRepository : CrudRepository<RefreshToken, String> {
    fun findByToken(token: String): Optional<RefreshToken>
}
