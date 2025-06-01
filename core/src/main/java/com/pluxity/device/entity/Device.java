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

    public void changeFeature(Feature feature) {
        // 기존 피처가 있고, 새 피처와 다르면 기존 피처에서 디바이스 참조 제거
        if (this.feature != null && this.feature != feature) {
            this.feature.changeDevice(null);
        }

        // 새 피처 설정
        this.feature = feature;

        // 새 피처가 있고, 새 피처의 디바이스가 현재 디바이스가 아니면 양방향 연관관계 설정
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

    // 자식 클래스에서 구현되는 디바이스 코드 조회 메서드
    public abstract String getDeviceCode();

    public void clearAllRelations() {
        // Feature와의 연관관계 제거
        if (this.feature != null) {
            this.changeFeature(null);
        }

        // DeviceCategory와의 연관관계 제거
        if (this.category != null) {
            this.updateCategory(null);
        }
    }
}
