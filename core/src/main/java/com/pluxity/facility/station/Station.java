package com.pluxity.facility.station;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.line.Line;
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

    @Column(name = "route")
    private String route;

    @Builder
    public Station(String name, String description, String route) {
        super(name, description);
        this.route = route;
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

    public void updateRoute(String route) {
        this.route = route;
    }
}
