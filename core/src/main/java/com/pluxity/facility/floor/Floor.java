package com.pluxity.facility.floor;

import com.pluxity.facility.facility.Facility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

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
    @NotFound(action = NotFoundAction.IGNORE)
    private Facility facility;

    @Column(name = "floor_id", nullable = false)
    private String floorId;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder
    public Floor(Facility facility, String floorId, String name) {
        this.facility = facility;
        this.floorId = floorId;
        this.name = name;
    }

    public void assignParent(Facility facility) {
        this.facility = facility;
    }
}
