package com.pluxity.facility;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilityPosition {

    private Double lon;

    private Double lat;

    @Column(columnDefinition = "text")
    private String locationMeta;

    @Builder
    public FacilityPosition(Double lon, Double lat, String locationMeta) {
        this.lon = lon;
        this.lat = lat;
        this.locationMeta = locationMeta;
    }

    public void merge(Double lon, Double lat, String locationMeta) {
        if (lon != null) this.lon = lon;
        if (lat != null) this.lat = lat;
        if (StringUtils.hasText(locationMeta)) this.locationMeta = locationMeta;
    }
}
