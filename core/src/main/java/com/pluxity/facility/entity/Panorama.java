package com.pluxity.facility.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "panorama")
@DiscriminatorValue("PANORAMA")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Panorama extends Facility {

    @Builder
    public Panorama(String name, String description, Long drawingFileId, Long thumbnailFileId, String geoJson) {
    }

}
