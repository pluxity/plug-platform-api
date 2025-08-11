package com.pluxity.cctv.entity;

import com.pluxity.device.entity.Device;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceCctv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cctv_id")
    private Cctv cctv;

    @Builder
    public DeviceCctv(Device device, Cctv cctv) {
        this.device = device;
        this.cctv = cctv;
    }
}
