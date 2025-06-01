package com.pluxity.domains.station;

import com.pluxity.facility.station.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE sasang_station SET deleted = true WHERE id = ?")
public class SasangStation extends Station {

    @Column(name = "external_code")
    private String externalCode;

    @Builder(builderMethodName = "sasangStationBuilder")
    public SasangStation(String name, String description, String route, String externalCode) {
        super(name, description, route);
        this.externalCode = externalCode;
    }

    public void updateExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }
}
