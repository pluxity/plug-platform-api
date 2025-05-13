package com.pluxity.device.repository;

import com.pluxity.device.entity.DefaultDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefaultDeviceRepository extends JpaRepository<DefaultDevice, Long> {}
