package com.pluxity.feature.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.device.entity.Device;
import com.pluxity.facility.facility.Facility;
import lombok.Getter;

@Getter
public class FeatureRelations {
    
    private Device device;
    private Asset asset;
    private Facility facility;
    private String floorId;

    FeatureRelations(Device device, Asset asset, Facility facility, String floorId) {
        this.device = device;
        this.asset = asset;
        this.facility = facility;
        this.floorId = floorId;
    }

    public static FeatureRelations create(Asset asset, Facility facility, String floorId) {
        return new FeatureRelations(null, asset, facility, floorId);
    }

    public static FeatureRelations empty() {
        return new FeatureRelations(null, null, null, null);
    }

    public static FeatureRelations of(Device device, Asset asset, Facility facility, String floorId) {
        return new FeatureRelations(device, asset, facility, floorId);
    }

    public FeatureRelations changeDevice(Device newDevice) {
        return new FeatureRelations(newDevice, this.asset, this.facility, this.floorId);
    }

    public FeatureRelations changeAsset(Asset newAsset) {
        return new FeatureRelations(this.device, newAsset, this.facility, this.floorId);
    }

    public FeatureRelations changeFacility(Facility newFacility) {
        return new FeatureRelations(this.device, this.asset, newFacility, this.floorId);
    }

    public FeatureRelations updateFloorId(String floorId) {
        return new FeatureRelations(this.device, this.asset, this.facility, floorId);
    }

    public void establishRelations(Feature feature) {
        if (device != null) {
            device.changeFeature(feature);
        }
        if (asset != null) {
            asset.addFeature(feature);
        }
        if (facility != null) {
            facility.addFeature(feature);
        }
    }

    public void clearRelations(Feature feature) {
        if (device != null) {
            device.clearFeatureOnly();
        }
        if (asset != null) {
            asset.removeFeature(feature);
        }
        if (facility != null) {
            facility.removeFeature(feature);
        }
    }

    public boolean hasDevice() {
        return device != null;
    }

    public boolean hasAsset() {
        return asset != null;
    }

    public boolean hasFacility() {
        return facility != null;
    }

    public boolean hasFloor() {
        return floorId != null && !floorId.trim().isEmpty();
    }

    public boolean isEmpty() {
        return device == null && asset == null && facility == null && 
               (floorId == null || floorId.trim().isEmpty());
    }
} 