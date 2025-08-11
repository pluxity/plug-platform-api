package com.pluxity.device.repository;

import com.pluxity.device.entity.Device;
import com.pluxity.feature.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DeviceRepository extends JpaRepository<Device, String> {
    @Modifying
    @Query("UPDATE Device d SET d.feature = NULL WHERE d.feature = :feature")
    void updateFeatureByFeature(Feature feature);
}
