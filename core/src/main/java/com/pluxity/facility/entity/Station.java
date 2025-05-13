package com.pluxity.facility.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Entity
@Table(name = "station")
@DiscriminatorValue("STATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ConditionalOnProperty(name = "facility.station.enabled", havingValue = "true")
public class Station extends Facility {
    @Builder
    public Station(String name, String description) {
        super(name, description);
    }
}