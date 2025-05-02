package com.pluxity.facility.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(LocationId.class)
public class Location {

    @Id
    @Column(name = "latitude")
    private Long latitude;

    @Id
    @Column(name = "longitude")
    private Long longitude;
}
