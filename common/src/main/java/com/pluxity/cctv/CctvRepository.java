package com.pluxity.cctv;

import com.pluxity.feature.entity.Feature;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CctvRepository extends JpaRepository<Cctv, String> {
    @EntityGraph(attributePaths = {"feature"})
    Optional<Cctv> findByFeature(Feature feature);
}
