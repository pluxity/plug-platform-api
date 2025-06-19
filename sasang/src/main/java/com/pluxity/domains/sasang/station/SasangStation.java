package com.pluxity.domains.sasang.station; // Updated package

import com.pluxity.facility.station.Station;
import jakarta.persistence.CascadeType; // Added
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType; // Added
import jakarta.persistence.GeneratedValue; // Added
import jakarta.persistence.GenerationType; // Added
import jakarta.persistence.Id; // Added
import jakarta.persistence.JoinColumn; // Added
import jakarta.persistence.OneToOne; // Added
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SasangStation { // Removed extends Station

    @Id // Added
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Added
    private Long id; // Added

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true) // Added
    @JoinColumn(name = "station_id", referencedColumnName = "id", nullable = false) // Added
    private Station station; // Added

    @Column(name = "external_code")
    private String externalCode;

    @Builder(builderMethodName = "sasangStationBuilder")
    public SasangStation(String name, String description, String route, String externalCode) {
        // super(name, description, route); // Removed
        this.station = new Station(name, description, route); // Added
        this.externalCode = externalCode;
    }

    public void updateExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }
}
