package com.pluxity.domains.device.repository;

import com.pluxity.domains.device.entity.Nflux;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NfluxRepository extends JpaRepository<Nflux, Long> {
    List<Nflux> findByCategoryId(Long categoryId);

    @Override
    @EntityGraph(attributePaths = {"feature", "feature.device", "category"})
    List<Nflux> findAll();

    Optional<Nflux> findByCode(String code);
}
