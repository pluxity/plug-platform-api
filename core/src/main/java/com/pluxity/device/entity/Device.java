package com.pluxity.device.entity;

import com.pluxity.feature.entity.Feature;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "device_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Device extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private DeviceCategory category;

    @Column(name = "name")
    protected String name;

    protected Device(Feature feature, DeviceCategory category) {
        this.feature = feature;
        if (this.feature != null) {
            this.feature.changeDevice(this);
        }
        this.category = category;
        if (this.category != null) {
            this.category.addDevice(this);
        }
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void changeFeature(Feature newFeature) {
        if (this.feature != null
                && this.feature.getDevice() != null
                && this.feature.getDevice().equals(this)) {
            this.feature.clearDeviceOnly();
        }
        this.feature = newFeature;

        if (newFeature != null
                && (newFeature.getDevice() == null || !newFeature.getDevice().equals(this))) {
            newFeature.assignDeviceOnly(this);
        }
    }

    public void assignFeatureOnly(Feature feature) {
        this.feature = feature;
    }

    public void clearFeatureOnly() {
        this.feature = null;
    }

    public void updateCategory(DeviceCategory category) {
        if (this.category != null) {
            this.category.removeDevice(this);
        }
        this.category = category;
        if (category != null) {
            category.addDevice(this);
        }
    }
}
