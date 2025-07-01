package com.pluxity.facility.facility;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facility_building_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuildingInfo {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Facility facility;

    private int floorCount;
    private String address;

    public BuildingInfo(Facility facility, int floorCount, String address) {
        this.facility = facility;
        this.floorCount = floorCount;
        this.address = address;
    }

    public void updateFloorCount(int floorCount) {
        this.floorCount = floorCount;
    }

    public void updateAddress(String address) {
        this.address = address;
    }
} 