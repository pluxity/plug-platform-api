package com.pluxity.station;

import com.pluxity.facility.Facility;
import jakarta.persistence.*;
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
    public Station(
            String name, String code, String description, Long drawingFileId, Long thumbnailFileId) {
        super(name, code, description, drawingFileId, thumbnailFileId);
    }
}
