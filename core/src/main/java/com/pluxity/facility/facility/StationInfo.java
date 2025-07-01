package com.pluxity.facility.facility;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facility_station_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StationInfo {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Facility facility;

    private String lineName;

    public StationInfo(Facility facility, String lineName) {
        this.facility = facility;
        this.lineName = lineName;
    }

    public void updateLineName(String lineName) {
        this.lineName = lineName;
    }
} 