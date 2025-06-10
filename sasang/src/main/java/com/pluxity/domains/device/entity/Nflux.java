package com.pluxity.domains.device.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.feature.entity.Feature;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nflux")
@DiscriminatorValue("nflux")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nflux extends Device {

    @Column(name = "name")
    private String name;

    @Builder
    public Nflux(String id, Feature feature, DeviceCategory category, String name) {
        super(id, feature, category);
        this.name = name;
    }

    public static Nflux create(String id, DeviceCategory category, Asset asset, String name) {
        return Nflux.builder().id(id).category(category).name(name).build();
    }

    public void update(DeviceCategory newCategory, Asset newAsset, String newName) {
        if (newCategory != null) {
            updateCategory(newCategory);
        }

        if (newName != null) {
            this.name = newName;
        }
    }

    public void updateName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
