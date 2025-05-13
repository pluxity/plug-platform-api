package com.pluxity.device.repository;

import com.pluxity.device.entity.DeviceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceCategoryRepository extends JpaRepository<DeviceCategory, Long> {
} 