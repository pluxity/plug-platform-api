package com.pluxity.facility.station;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.line.Line;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "station_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StationLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Facility station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    @Builder
    public StationLine(Facility station, Line line) {
        this.station = station;
        this.line = line;
    }

    // 역할을 하는 시설물인지 확인하는 메서드
    public boolean isStationFacility() {
        return station != null && 
               (station.getFacilityType().equals(com.pluxity.facility.facility.FacilityType.STATION));
    }
}
