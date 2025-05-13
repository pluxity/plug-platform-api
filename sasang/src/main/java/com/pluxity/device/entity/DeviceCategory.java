package com.pluxity.device.entity;

import com.pluxity.category.entity.Category;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceCategory extends Category<DeviceCategory> {

    @OneToMany(
            mappedBy = "category",
            targetEntity = DefaultDevice.class,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final List<DefaultDevice> devices = new ArrayList<>();

    @Builder
    public DeviceCategory(String name, DeviceCategory parent) {
        this.name = name;
        this.parent = parent;
        this.validateDepth();
    }

    @Override
    public int getMaxDepth() {
        return 3;
    }

    public void addDevice(DefaultDevice device) {
        if (device != null && !this.devices.contains(device)) {
            this.devices.add(device);
        }
    }

    public void removeDevice(DefaultDevice device) {
        if (device != null) {
            this.devices.remove(device);
        }
    }
}
