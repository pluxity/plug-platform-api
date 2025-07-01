package com.pluxity.domains.station;

import com.pluxity.facility.facility.Facility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sasang_station_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SasangStationDetails {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Facility facility;

    @Column(name = "route", columnDefinition = "text")
    private String route;

    @Column(name = "subway", columnDefinition = "text")
    private String subway;

    @Column(name = "external_code")
    private String externalCode;

    @Column(name = "platform_count")
    private Integer platformCount;

    @Column(name = "is_transfer_station")
    private Boolean isTransferStation;

    @Builder
    public SasangStationDetails(Facility facility, String route, String subway, String externalCode, Integer platformCount, Boolean isTransferStation) {
        this.facility = facility;
        this.route = route;
        this.subway = subway;
        this.externalCode = externalCode;
        this.platformCount = platformCount;
        this.isTransferStation = isTransferStation;
    }

    public void updateExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public void updateRoute(String route) {
        this.route = route;
    }

    public void updateSubway(String subway) {
        this.subway = subway;
    }

    public void updatePlatformCount(Integer platformCount) {
        this.platformCount = platformCount;
    }

    public void updateIsTransferStation(Boolean isTransferStation) {
        this.isTransferStation = isTransferStation;
    }
} 