package com.pluxity.facility.line;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityType;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationLine;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "line")
@Getter
@RequiredArgsConstructor
public class Line extends BaseEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "line")
    private final List<StationLine> stationLines = new ArrayList<>();

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "color")
    private String color;

    @Builder
    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public void addStationFacility(Facility facility) {
        if (facility.getFacilityType() == FacilityType.STATION) {
            StationLine stationLine = StationLine.builder().station(facility).line(this).build();
            this.stationLines.add(stationLine);
        }
    }

    public void removeStationFacility(Facility facility) {
        stationLines.removeIf(stationLine -> stationLine.getStation().equals(facility));
    }

    // 기존 Station과의 호환성을 위한 메서드
    public void addStation(Station station) {
        addStationFacility(station);
    }

    public void removeStation(Station station) {
        removeStationFacility(station);
    }

    public List<Facility> getStationFacilities() {
        return stationLines.stream()
                .map(StationLine::getStation)
                .filter(facility -> facility.getFacilityType() == FacilityType.STATION)
                .toList();
    }

    // 기존 호환성을 위한 메서드 (Station 타입만 반환)
    public List<Station> getStations() {
        return stationLines.stream()
                .map(StationLine::getStation)
                .filter(facility -> facility instanceof Station)
                .map(facility -> (Station) facility)
                .toList();
    }

    public void update(Line line) {
        if (line.color != null) {
            this.color = line.color;
        }

        if (line.name != null) {
            this.name = line.name;
        }
    }
}
