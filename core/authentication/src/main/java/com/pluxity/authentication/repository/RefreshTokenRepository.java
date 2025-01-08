package com.pluxity.authentication.repository;

import com.pluxity.authentication.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByUsername(String findByUsername);

    Optional<RefreshToken> findByToken(String token);
}
