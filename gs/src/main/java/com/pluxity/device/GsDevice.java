package com.pluxity.device;

import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.feature.entity.Feature;
import com.pluxity.user.entity.Permissible;
import com.pluxity.user.entity.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gs_device")
@DiscriminatorValue("gs_device")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GsDevice extends Device implements Permissible {

    private String name;

    @Override
    public String getName() {
        return this.name;
    }

    @Builder
    public GsDevice(String id, Feature feature, DeviceCategory category, String name) {
        super(id, feature, category);
        this.name = name;
    }

    public void update(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    @Override
    public String getResourceId() {
        return String.valueOf(this.getCategory().getId());
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.DEVICE;
    }
}
