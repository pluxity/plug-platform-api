package com.pluxity.device;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GsDeviceRepository extends JpaRepository<GsDevice, String> {
    Optional<GsDevice> findByName(String name);
}
