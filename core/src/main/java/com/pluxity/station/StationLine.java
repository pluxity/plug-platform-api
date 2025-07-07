package com.pluxity.station;

import com.pluxity.global.entity.BaseEntity;
import com.pluxity.line.Line;
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
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    @Builder
    public StationLine(Station station, Line line) {
        this.station = station;
        this.line = line;
    }
}
