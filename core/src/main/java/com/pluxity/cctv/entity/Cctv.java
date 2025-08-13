package com.pluxity.cctv.entity;

import com.pluxity.cctv.dto.CctvUpdateRequest;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
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

    @Builder
    public Cctv(String id, String name, String url, DeviceCategory category) {
        super(id, category);
        this.name = name;
        this.url = url;
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
