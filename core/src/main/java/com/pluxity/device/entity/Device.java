package com.pluxity.device.entity;

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

    protected Device(Feature feature) {
        this.feature = feature;
        if (this.feature != null) {
            this.feature.changeDevice(this);
        }
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
}