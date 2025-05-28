package com.pluxity.feature.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.device.entity.Device;
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

    @Id
    private String id;

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

    @Builder
    public Feature(String id, Spatial position, Spatial rotation, Spatial scale, Asset asset) {
        this.id = id;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        if (asset != null) {
            changeAsset(asset);
        }
    }

    public static Feature create(FeatureCreateRequest request, String uuid) {
        return Feature.builder()
                .id(uuid)
                .position(request.position() != null ? request.position() : new Spatial(0.0, 0.0, 0.0))
                .rotation(request.rotation() != null ? request.rotation() : new Spatial(0.0, 0.0, 0.0))
                .scale(request.scale() != null ? request.scale() : new Spatial(1.0, 1.0, 1.0))
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
        this.device = device;
    }

    public void changeAsset(Asset newAsset) {
        // 기존 Asset과의 관계 해제
        if (this.asset != null) {
            this.asset.getFeatures().remove(this);
        }
        this.asset = newAsset;
        // 새로운 Asset과의 관계 설정
        if (newAsset != null && !newAsset.getFeatures().contains(this)) {
            newAsset.getFeatures().add(this);
        }
    }
}
