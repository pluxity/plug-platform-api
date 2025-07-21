package com.pluxity.park;

import com.pluxity.facility.Facility;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Entity
@Table(name = "park")
@DiscriminatorValue("PARK")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ConditionalOnProperty(name = "facility.park.enabled", havingValue = "true")
public class Park extends Facility {

    private String boundary;

    @Builder
    public Park(
            String name,
            String code,
            String description,
            Long drawingFileId,
            Long thumbnailFileId,
            String boundary) {
        super(name, code, description, drawingFileId, thumbnailFileId);
        this.boundary = boundary;
    }

    public void update(String boundary) {
        if (boundary != null) {
            this.boundary = boundary;
        }
    }
}
