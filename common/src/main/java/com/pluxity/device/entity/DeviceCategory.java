package com.pluxity.device.entity;

import com.pluxity.category.entity.Category;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device_category")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CATEGORY_TYPE")
@DiscriminatorValue("DEVICE_BASE")
@Getter
@NoArgsConstructor
public class DeviceCategory extends Category<DeviceCategory> {

    @OneToMany(mappedBy = "category") // Persist ALL 하면 생성할때 id 중복되서 오류 발생 가능
    private final List<Device> devices = new ArrayList<>();

    @Column(name = "icon_file_id")
    private Long iconFileId;

    @Builder
    public DeviceCategory(String name, Long iconFileId) {
        this.name = name;
        this.iconFileId = iconFileId;
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
}
