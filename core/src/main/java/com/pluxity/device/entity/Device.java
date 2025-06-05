package com.pluxity.device.entity;

import com.pluxity.feature.entity.Feature;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "device")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "device_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Device extends BaseEntity implements Persistable<String> {

    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private DeviceCategory category;

    @Transient
    private boolean isNew = true;

    protected Device(String id, Feature feature, DeviceCategory category) {
        this.id = id;
        this.feature = feature;
        if (this.feature != null) {
            this.feature.changeDevice(this);
        }
        this.category = category;
        if (this.category != null) {
            this.category.addDevice(this);
        }
        this.isNew = true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    public void changeFeature(Feature feature) {
        if (this.feature != null && this.feature != feature) {
            this.feature.changeDevice(null);
        }

        this.feature = feature;

        if (feature != null && feature.getDevice() != this) {
            feature.changeDevice(this);
        }
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

    public String getDeviceCode() {
        return this.id;
    }

    public void clearAllRelations() {
        if (this.feature != null) {
            this.changeFeature(null);
        }

        if (this.category != null) {
            this.updateCategory(null);
        }
    }
}
