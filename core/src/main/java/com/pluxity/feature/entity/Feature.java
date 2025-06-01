package com.pluxity.feature.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.device.entity.Device;
import com.pluxity.facility.facility.Facility;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "feature")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Feature extends BaseEntity {

    @Id private String id;

    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "position_x"))
    @AttributeOverride(name = "y", column = @Column(name = "position_y"))
    @AttributeOverride(name = "z", column = @Column(name = "position_z"))
    private Spatial position;

    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "rotation_x"))
    @AttributeOverride(name = "y", column = @Column(name = "rotation_y"))
    @AttributeOverride(name = "z", column = @Column(name = "rotation_z"))
    private Spatial rotation;

    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "scale_x"))
    @AttributeOverride(name = "y", column = @Column(name = "scale_y"))
    @AttributeOverride(name = "z", column = @Column(name = "scale_z"))
    private Spatial scale;

    @OneToOne(mappedBy = "feature", cascade = CascadeType.PERSIST)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(name = "floor_id")
    private String floorId;

    @Builder
    public Feature(
            String id,
            Spatial position,
            Spatial rotation,
            Spatial scale,
            Asset asset,
            Facility facility,
            String floorId) {
        this.id = id;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.floorId = floorId;
        this.asset = asset;
        this.facility = facility;
    }

    public static Feature create(FeatureCreateRequest request, String uuid) {
        return Feature.builder()
                .id(uuid)
                .position(request.position() != null ? request.position() : new Spatial(0.0, 0.0, 0.0))
                .rotation(request.rotation() != null ? request.rotation() : new Spatial(0.0, 0.0, 0.0))
                .scale(request.scale() != null ? request.scale() : new Spatial(1.0, 1.0, 1.0))
                .floorId(request.floorId())
                .build();
    }

    public void update(FeatureUpdateRequest request) {
        if (request.position() != null) {
            this.position = request.position();
        }
        if (request.rotation() != null) {
            this.rotation = request.rotation();
        }
        if (request.scale() != null) {
            this.scale = request.scale();
        }
    }

    public void updatePosition(Spatial position) {
        if (position != null) {
            this.position = position;
        }
    }

    public void updateRotation(Spatial rotation) {
        if (rotation != null) {
            this.rotation = rotation;
        }
    }

    public void updateScale(Spatial scale) {
        if (scale != null) {
            this.scale = scale;
        }
    }

    public boolean isDefaultPosition() {
        return position != null
                && position.getX() == 0.0
                && position.getY() == 0.0
                && position.getZ() == 0.0;
    }

    public boolean isDefaultScale() {
        return scale != null && scale.getX() == 1.0 && scale.getY() == 1.0 && scale.getZ() == 1.0;
    }

    public boolean isDefaultRotation() {
        return rotation != null
                && rotation.getX() == 0.0
                && rotation.getY() == 0.0
                && rotation.getZ() == 0.0;
    }

    public void changeDevice(Device device) {
        if (this.device != null && this.device.getFeature() != null) {
            this.device.clearFeatureOnly();
        }

        this.device = device;

        if (device != null && device.getFeature() != this) {
            device.changeFeature(this);
        }
    }

    public void changeAsset(Asset asset) {
        if (this.asset != null) {
            this.asset.removeFeature(this);
        }

        this.asset = asset;

        if (asset != null) {
            asset.addFeature(this);
        }
    }

    public void changeFacility(Facility facility) {
        if (this.facility != null) {}

        this.facility = facility;

        if (facility != null) {
            // 필요한 경우 시설 측의 연관관계 설정 로직 추가
        }
    }

    /** 디바이스 관계만 단방향으로 제거하는 메서드 (디바이스에서 피처 제거 시 사용) */
    public void clearDeviceOnly() {
        this.device = null;
    }

    /** 에셋 관계만 단방향으로 제거하는 메서드 (에셋에서 피처 제거 시 사용) */
    public void clearAssetOnly() {
        this.asset = null;
    }

    /** 모든 연관관계를 제거하는 메서드 */
    public void clearAllRelations() {
        // Asset 연관관계 제거
        if (this.asset != null) {
            this.changeAsset(null);
        }

        // Facility 연관관계 제거
        if (this.facility != null) {
            this.changeFacility(null);
        }

        // Device 연관관계 제거
        if (this.device != null) {
            this.changeDevice(null);
        }
    }
}
