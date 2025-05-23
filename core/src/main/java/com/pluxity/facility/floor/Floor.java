package com.pluxity.facility.floor;

import com.pluxity.facility.facility.Facility;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "floor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(name = "group_id", nullable = false)
    private Integer groupId;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder
    public Floor(Facility facility, Integer groupId, String name) {
        this.facility = facility;
        this.groupId = groupId;
        this.name = name;
    }

    public void assignParent(Facility facility) {
        this.facility = facility;
    }
}
