package com.pluxity.facility.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "panorama")
@DiscriminatorValue("PANORAMA")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Panorama extends Facility {

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Builder
    public Panorama(String name, String description, String address, Double latitude, Double longitude) {
        super(name, description);
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
