package com.pluxity.device.repository;

import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.global.annotation.CheckPermissionAll;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceCategoryRepository extends JpaRepository<DeviceCategory, Long> {
    List<DeviceCategory> findByParentId(Long parentId);

    @CheckPermissionAll(resourceName = "DEVICE_CATEGORY")
    List<DeviceCategory> findAll(Sort sort);
}
