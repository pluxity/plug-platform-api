package com.pluxity.facility.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    @Builder
    public Station(String name, String description) {
        super(name, description);
    }

    public void changeLine(Line newLine) {
        if (this.line != null) {
            this.line.getStations().remove(this);
        }

        this.line = newLine;

        if (newLine != null) {
            if (!newLine.getStations().contains(this)) {
                newLine.getStations().add(this);
            }
        }
    }
}
