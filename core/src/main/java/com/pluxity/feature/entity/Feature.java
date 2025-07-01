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

    @Id 
    private String id;

    @Embedded
    @AttributeOverride(name = "position.x", column = @Column(name = "position_x"))
    @AttributeOverride(name = "position.y", column = @Column(name = "position_y"))
    @AttributeOverride(name = "position.z", column = @Column(name = "position_z"))
    @AttributeOverride(name = "rotation.x", column = @Column(name = "rotation_x"))
    @AttributeOverride(name = "rotation.y", column = @Column(name = "rotation_y"))
    @AttributeOverride(name = "rotation.z", column = @Column(name = "rotation_z"))
    @AttributeOverride(name = "scale.x", column = @Column(name = "scale_x"))
    @AttributeOverride(name = "scale.y", column = @Column(name = "scale_y"))
    @AttributeOverride(name = "scale.z", column = @Column(name = "scale_z"))
    private SpatialTransform transform;

    @OneToOne(mappedBy = "feature")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(name = "floor_id")
    private String floorId;

    @Transient
    private FeatureRelations relations;

    @Builder
    public Feature(String id, SpatialTransform transform, Asset asset, Facility facility, String floorId) {
        this.id = id;
        this.transform = transform != null ? transform : SpatialTransform.createDefault();
        this.asset = asset;
        this.facility = facility;
        this.floorId = floorId;
        initializeRelations();
    }

    public static Feature create(FeatureCreateRequest request, String uuid) {
        SpatialTransform transform = SpatialTransform.create(
                request.position(), 
                request.rotation(), 
                request.scale()
        );
        
        return Feature.builder()
                .id(uuid)
                .transform(transform)
                .floorId(request.floorId())
                .build();
    }

    @PostLoad
    private void initializeRelations() {
        this.relations = FeatureRelations.of(device, asset, facility, floorId);
    }

    public void updateTransform(FeatureUpdateRequest request) {
        if (request.position() != null) {
            this.transform = this.transform.updatePosition(request.position());
        }
        if (request.rotation() != null) {
            this.transform = this.transform.updateRotation(request.rotation());
        }
        if (request.scale() != null) {
            this.transform = this.transform.updateScale(request.scale());
        }
    }

    public void updatePosition(Spatial position) {
        this.transform = this.transform.updatePosition(position);
    }

    public void updateRotation(Spatial rotation) {
        this.transform = this.transform.updateRotation(rotation);
    }

    public void updateScale(Spatial scale) {
        this.transform = this.transform.updateScale(scale);
    }

    public void changeDevice(Device newDevice) {
        clearDeviceRelation();
        this.device = newDevice;
        this.relations = this.relations.changeDevice(newDevice);
        if (newDevice != null) {
            newDevice.changeFeature(this);
        }
    }

    public void changeAsset(Asset newAsset) {
        clearAssetRelation();
        this.asset = newAsset;
        this.relations = this.relations.changeAsset(newAsset);
        if (newAsset != null) {
            newAsset.addFeature(this);
        }
    }

    public void changeFacility(Facility newFacility) {
        clearFacilityRelation();
        this.facility = newFacility;
        this.relations = this.relations.changeFacility(newFacility);
        if (newFacility != null) {
            newFacility.addFeature(this);
        }
    }

    public void updateFloorId(String floorId) {
        this.floorId = floorId;
        this.relations = this.relations.updateFloorId(floorId);
    }

    public void clearAllRelations() {
        clearDeviceRelation();
        clearAssetRelation();
        clearFacilityRelation();
        this.relations = FeatureRelations.empty();
    }

    public Spatial getPosition() {
        return transform.getPosition();
    }

    public Spatial getRotation() {
        return transform.getRotation();
    }

    public Spatial getScale() {
        return transform.getScale();
    }

    public boolean isDefaultPosition() {
        return transform.isDefaultPosition();
    }

    public boolean isDefaultRotation() {
        return transform.isDefaultRotation();
    }

    public boolean isDefaultScale() {
        return transform.isDefaultScale();
    }

    public boolean isCompletelyDefault() {
        return transform.isCompletelyDefault();
    }

    public boolean hasDevice() {
        return relations != null ? relations.hasDevice() : device != null;
    }

    public boolean hasAsset() {
        return relations != null ? relations.hasAsset() : asset != null;
    }

    public boolean hasFacility() {
        return relations != null ? relations.hasFacility() : facility != null;
    }

    public boolean hasFloor() {
        return relations != null ? relations.hasFloor() : (floorId != null && !floorId.trim().isEmpty());
    }

    private void clearDeviceRelation() {
        if (this.device != null) {
            this.device.clearFeatureOnly();
            this.device = null;
        }
    }

    private void clearAssetRelation() {
        if (this.asset != null) {
            this.asset.removeFeature(this);
            this.asset = null;
        }
    }

    private void clearFacilityRelation() {
        if (this.facility != null) {
            this.facility.removeFeature(this);
            this.facility = null;
        }
    }
}
