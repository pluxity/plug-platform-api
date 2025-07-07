package com.pluxity.panorama;

import com.pluxity.facility.Facility;
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

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "altitude")
    private Double altitude;

    @Builder
    public Panorama(
            String name, String description, Double latitude, Double longitude, Double altitude) {
        super(name, description);
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }
}
