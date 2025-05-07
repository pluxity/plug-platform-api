package com.pluxity.facility.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "building")
@DiscriminatorValue("BUILDING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Building extends Facility {

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Floor> floors = new ArrayList<>();

    @Builder
    public Building(String name, String description,
                    String address, Double latitude, Double longitude) {
        super(name, description);
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
