package com.pluxity.device;

import com.pluxity.feature.entity.Feature;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GsDeviceRepository extends JpaRepository<GsDevice, String> {

    @EntityGraph(attributePaths = {"feature"})
    Optional<GsDevice> findByFeature(Feature feature);
}
