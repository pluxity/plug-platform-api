package com.pluxity.domains.device.repository;

import com.pluxity.domains.device.entity.Nflux;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NfluxRepository extends JpaRepository<Nflux, String> {
    List<Nflux> findByCategoryId(Long categoryId);

    @Override
    @EntityGraph(attributePaths = {"feature", "feature.device", "category"})
    List<Nflux> findAll();
}
