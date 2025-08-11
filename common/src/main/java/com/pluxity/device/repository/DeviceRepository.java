package com.pluxity.device.repository;

import com.pluxity.device.entity.Device;
import com.pluxity.feature.entity.Feature;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, String> {
    Optional<Device> findByFeature(Feature feature);
}
