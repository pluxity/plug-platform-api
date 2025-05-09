package com.pluxity.facility.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "building")
@DiscriminatorValue("BUILDING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Building extends Facility {

    @Builder
    public Building(String name, String description) {
        super(name, description);
    }
}
