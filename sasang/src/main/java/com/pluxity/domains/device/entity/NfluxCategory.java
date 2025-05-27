package com.pluxity.domains.device.entity;

import com.pluxity.device.entity.DeviceCategory;
import jakarta.persistence.Entity;

@Entity
public class NfluxCategory extends DeviceCategory {

    String contextPath;

    @Override
    public int getMaxDepth() {
        return 1;
    }
}
