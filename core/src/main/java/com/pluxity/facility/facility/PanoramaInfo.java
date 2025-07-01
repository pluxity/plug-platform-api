package com.pluxity.facility.facility;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facility_panorama_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PanoramaInfo {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Facility facility;

    private String panoramaUrl;

    public PanoramaInfo(Facility facility, String panoramaUrl) {
        this.facility = facility;
        this.panoramaUrl = panoramaUrl;
    }

    public void updatePanoramaUrl(String panoramaUrl) {
        this.panoramaUrl = panoramaUrl;
    }
} 