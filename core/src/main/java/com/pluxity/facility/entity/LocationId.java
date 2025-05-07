package com.pluxity.facility.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class LocationId implements Serializable {
    private Double latitude;
    private Double longitude;
}