package com.pluxity.domains.device.repository;

import com.pluxity.domains.device.entity.DefaultDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefaultDeviceRepository extends JpaRepository<DefaultDevice, Long> {}
