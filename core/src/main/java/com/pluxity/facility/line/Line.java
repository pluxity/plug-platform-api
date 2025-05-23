package com.pluxity.facility.line;

import com.pluxity.facility.station.Station;
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

    @OneToMany(mappedBy = "line", cascade = CascadeType.PERSIST)
    private final List<Station> stations = new ArrayList<>();

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;

    @Builder
    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public void addStation(Station station) {
        if (station.getLine() != this) {
            station.changeLine(this);
        }
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
