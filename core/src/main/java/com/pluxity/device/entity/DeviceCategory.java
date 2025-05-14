package com.pluxity.device.entity;

import com.pluxity.category.entity.Category;
import com.pluxity.icon.entity.Icon;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private Icon icon;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Device> devices = new ArrayList<>();

    @Builder
    public DeviceCategory(String name, DeviceCategory parent) {
        this.name = name;
        if (parent != null) {
            this.assignToParent(parent);
        } else {
            this.parent = null;
        }
        this.validateDepth();
    }

    @Override
    public int getMaxDepth() {
        return 3;
    }

    public void updateIcon(Icon icon) {
        this.icon = icon;
    }

    public void addDevice(Device device) {
        if (device != null && !this.devices.contains(device)) {
            this.devices.add(device);
        }
    }

    public void removeDevice(Device device) {
        if (device != null) {
            this.devices.remove(device);
        }
    }
}
