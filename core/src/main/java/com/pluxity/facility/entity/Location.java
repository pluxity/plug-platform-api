package com.pluxity.facility.entity;

import com.pluxity.facility.dto.LocationRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(LocationId.class)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "altitude")
    private Double altitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Builder
    public Location(Facility facility, Double latitude, Double longitude, Double altitude) {
        this.facility = facility;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public void update(LocationRequest data) {
        if(data.latitude() != null) {
            this.latitude = data.latitude();
        }
        if(data.longitude() != null) {
            this.longitude = data.longitude();
        }
        if(data.altitude() != null) {
            this.altitude = data.altitude();
        }
    }
}
