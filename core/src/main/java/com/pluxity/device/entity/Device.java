package com.pluxity.device.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.facility.entity.Facility;
import com.pluxity.feature.entity.Feature;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
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
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private DeviceCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(name = "name")
    protected String name;

    protected Device(Feature feature, Asset asset, DeviceCategory category, Facility facility) {
        this.asset = asset;
        this.feature = feature;
        if (this.feature != null) {
            this.feature.changeDevice(this);
        }
        this.category = category;
        this.facility = facility;
        if (this.category != null) {
            this.category.addDevice(this);
        }
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void changeAsset(Asset asset) {
        this.asset = asset;
    }

    public void changeFeature(Feature newFeature) {
        if (this.feature != null) {
            this.feature.changeDevice(null);
        }
        this.feature = newFeature;
        if (newFeature != null && newFeature.getDevice() != this) {
            newFeature.changeDevice(this);
        }
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

    public void updateFacility(Facility facility) {
        this.facility = facility;
    }
}
