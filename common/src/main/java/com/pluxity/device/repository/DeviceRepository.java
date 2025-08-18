package com.pluxity.device.repository;

import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.facility.Facility;
import com.pluxity.feature.entity.Feature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DeviceRepository extends JpaRepository<Device, String> {
    @Modifying
    @Query("UPDATE Device d SET d.feature = NULL WHERE d.feature = :feature")
    void updateFeatureByFeature(Feature feature);

    @Query("SELECT d FROM Device d WHERE d.category = :category AND d.feature.facility = :facility")
    List<Device> findByCategoryAndFacility(DeviceCategory category, Facility facility);
}
