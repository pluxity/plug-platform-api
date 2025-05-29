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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CATEGORY_TYPE")
@DiscriminatorValue("DEVICE_BASE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceCategory extends Category<DeviceCategory> {

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Device> devices = new ArrayList<>();

    @Column(name = "icon_file_id")
    private Long iconFileId;

    @Column(name = "DTYPE", insertable = true, updatable = false)
    private String dtype = "DEVICE_BASE";

    private final String prefix = "device-categories/";

    @Builder
    public DeviceCategory(String name, DeviceCategory parent) {
        this.name = name;
        changeParent(parent);
    }

    @Override
    public int getMaxDepth() {
        return 3;
    }

    public void updateIconFileId(Long iconFileId) {
        this.iconFileId = iconFileId;
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

    public void changeParent(DeviceCategory newParent) {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }
        this.parent = newParent;
        if (newParent != null) {
            newParent.getChildren().add(this);
        }
        this.validateDepth();
    }

    public String getPrefix() {
        return this.prefix + this.iconFileId + "/";
    }
}
