package com.pluxity.facility.station;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.line.Line;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<StationLine> stationLines = new ArrayList<>();

    @Column(name = "route")
    private String route;

    @Builder
    public Station(String name, String description, String route) {
        super(name, description);
        this.route = route;
    }

    public void addLine(Line line) {
        StationLine stationLine = StationLine.builder().station(this).line(line).build();

        this.stationLines.add(stationLine);
        line.getStationLines().add(stationLine);
    }

    public void removeLine(Line line) {
        stationLines.removeIf(
                stationLine -> {
                    if (stationLine.getLine().equals(line)) {
                        line.getStationLines().remove(stationLine);
                        return true;
                    }
                    return false;
                });
    }

    public void updateRoute(String route) {
        this.route = route;
    }
}
