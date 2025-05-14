package com.pluxity.facility.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class LocationId implements Serializable {
    private Double latitude;
    private Double longitude;
}
