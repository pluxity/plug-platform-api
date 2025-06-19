package com.pluxity.facility.station;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.line.Line;
import jakarta.persistence.*; // Keep existing
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Entity
@Table(name = "station")
// @DiscriminatorValue("STATION") // Removed
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ConditionalOnProperty(name = "facility.station.enabled", havingValue = "true")
public class Station { // Removed "extends Facility"

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "facility_id", referencedColumnName = "id", nullable = false)
    private Facility facility;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<StationLine> stationLines = new ArrayList<>();

    @Column(name = "route", columnDefinition = "text")
    private String route;

    @Column(name = "subway", columnDefinition = "text")
    private String subway;

    @Builder
    public Station(String name, String description, String route) {
        // super(name, description); // Removed
        this.facility = new Facility(name, description, "Initial station facility"); // Added
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
