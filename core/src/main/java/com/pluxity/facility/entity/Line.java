package com.pluxity.facility.entity;

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
public class Line {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL)
    private final List<Station> stations = new ArrayList<>();

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;

    @Column(name = "route")
    private String route;

    @Builder
    public Line(String name, String color, String route) {
        this.name = name;
        this.color = color;
        this.route = route;
    }

    public static Line create(String name, String color, String route) {
        return Line.builder().color(color).name(name).route(route).build();
    }

    public void addStation(Station station) {
        if (station.getLine() != this) {
            station.changeLine(this);
        }
    }

    public void update(Line line) {
        if (color != null) {
            this.color = line.color;
        }

        if (name != null) {
            this.name = line.name;
        }

        if (route != null) {
            this.route = line.route;
        }
    }
}
