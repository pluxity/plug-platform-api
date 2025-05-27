package com.pluxity.domains.station;

import com.pluxity.facility.station.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SasangStation extends Station {

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "external_code")
    private String externalCode;

    @Builder(builderMethodName = "sasangStationBuilder")
    public SasangStation(
            String name, String description, String route, String code, String externalCode) {
        super(name, description, route);
        this.code = code;
        this.externalCode = externalCode;
    }

    public void updateCode(String code) {
        this.code = code;
    }

    public void updateExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }
}
