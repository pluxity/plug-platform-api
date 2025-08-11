package com.pluxity.cctv.entity;

import com.pluxity.cctv.dto.CctvUpdateRequest;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.feature.entity.Feature;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("cctv")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cctv extends Device {

    private String name;

    @Column(length = 1000)
    private String url;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @Builder
    public Cctv(String id, String name, String url, DeviceCategory category, Feature feature) {
        super(id, category);
        this.name = name;
        this.url = url;
        this.feature = feature;
    }

    public void updateCctv(CctvUpdateRequest request) {
        this.name = request.name();
        this.url = request.url();
    }

    @Override
    public String getName() {
        return this.name;
    }
}
