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
@Table(name = "building")
@DiscriminatorValue("BUILDING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ConditionalOnProperty(name = "facility.building.enabled", havingValue = "true")
public class Building extends Facility {
    @Builder
    public Building(String name, String description) {
        super(name, description);
    }
}
